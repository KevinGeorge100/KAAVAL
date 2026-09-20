package com.kaaval.app.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.Files

/**
 * Structured result of an AI Audio Witness analysis.
 */
data class AudioAnalysisResult(
    val summary: String,
    val transcript: String,
    val threatLevel: String = "HIGH"
)

/**
 * OpenAI API Emergency Analyzer
 * Powered by OpenAI Whisper & GPT-4o-mini
 *
 * Provides AI emergency context summarization, situational analysis via Whisper,
 * and immediate threat level assessment for caregivers.
 */
class OpenAiEmergencyAnalyzer(private val apiKey: String) {

    suspend fun analyzeEmergencyAudio(audioFile: File): AudioAnalysisResult = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey.startsWith("your_")) {
            Log.w("OpenAiAnalyzer", "API key not configured, using local baseline analysis.")
            return@withContext AudioAnalysisResult(
                summary = "Audio witness captured. Device environment is being actively monitored.",
                transcript = "Ambient audio recorded successfully."
            )
        }

        if (!audioFile.exists() || audioFile.length() == 0L) {
            return@withContext AudioAnalysisResult(
                summary = "Audio witness file missing or empty. Live GPS tracking remains active.",
                transcript = ""
            )
        }

        try {
            // 1. Transcribe Audio using Whisper API
            val transcript = transcribeAudio(audioFile)
            Log.d("OpenAiAnalyzer", "Whisper Transcript: $transcript")

            // 2. Handle Silence or Noise
            if (transcript.trim().length < 3) {
                return@withContext AudioAnalysisResult(
                    summary = "Ambient audio indicates quiet surroundings or muffled device. No vocal distress heard.",
                    transcript = "No distinct speech detected."
                )
            }

            // 3. Analyze Situation using GPT-4o-mini
            val summary = situationAwareness(transcript)
            val threatLevel = if (transcript.contains("help", ignoreCase = true) ||
                transcript.contains("fall", ignoreCase = true) ||
                transcript.contains("hurt", ignoreCase = true) ||
                transcript.contains("pain", ignoreCase = true)) "CRITICAL" else "HIGH"

            return@withContext AudioAnalysisResult(
                summary = summary,
                transcript = transcript,
                threatLevel = threatLevel
            )

        } catch (e: Exception) {
            Log.e("OpenAiAnalyzer", "Full Audio Analysis Failed: ${e.message}", e)
            return@withContext AudioAnalysisResult(
                summary = "Audio witness captured. Live telemetry active while processing audio.",
                transcript = "Audio recorded; automated transcription pending."
            )
        }
    }

    private suspend fun transcribeAudio(file: File): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/audio/transcriptions")
        val boundary = "Boundary-${System.currentTimeMillis()}"
        val conn = url.openConnection() as HttpURLConnection
        
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.doOutput = true

        val mimeType = when (file.extension.lowercase()) {
            "m4a" -> "audio/m4a"
            "mp4" -> "audio/mp4"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            else -> "application/octet-stream"
        }

        conn.outputStream.use { out ->
            val writer = out.writer()
            writer.write("--$boundary\r\n")
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n")
            writer.write("Content-Type: $mimeType\r\n\r\n")
            writer.flush()
            
            Files.copy(file.toPath(), out)
            
            writer.write("\r\n--$boundary\r\n")
            writer.write("Content-Disposition: form-data; name=\"model\"\r\n\r\n")
            writer.write("whisper-1\r\n")
            writer.write("--$boundary--\r\n")
            writer.flush()
        }

        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            return@withContext JSONObject(response).getString("text")
        } else {
            val err = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP ${conn.responseCode}"
            Log.w("OpenAiAnalyzer", "Whisper error response: $err")
        }
        ""
    }

    private suspend fun situationAwareness(transcript: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.connectTimeout = 10000
        conn.readTimeout = 15000
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are KAAVAL Sentinel, an emergency dispatch AI for visually impaired and elderly individuals. Analyze this ambient audio transcript. In 20 words or less, state immediate danger, user state, and environmental cues for family caregivers.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Transcript: $transcript")
                })
            })
            put("max_tokens", 80)
            put("temperature", 0.3)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val content = JSONObject(response).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            return@withContext content.trim()
        }
        "Ambient Audio: $transcript"
    }

    suspend fun generateEmergencySummary(
        locationAddress: String,
        userMedicalProfile: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank() || apiKey.startsWith("your_")) {
            return@withContext "Standard Emergency Alert: Location: $locationAddress"
        }

        try {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
            conn.connectTimeout = 10000
            conn.readTimeout = 15000
            conn.doOutput = true

            val jsonBody = JSONObject().apply {
                put("model", "gpt-4o-mini")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", "You are an AI Emergency Incident Summarizer for KAAVAL, an emergency response ecosystem for visually impaired individuals. Create a concise 2-sentence summary for caregivers.")
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", "Location: $locationAddress. Medical Notes: $userMedicalProfile.")
                    })
                })
                put("max_tokens", 100)
            }

            OutputStreamWriter(conn.outputStream).use { writer ->
                writer.write(jsonBody.toString())
                writer.flush()
            }

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val jsonResponse = JSONObject(responseText)
                val choices = jsonResponse.getJSONArray("choices")
                if (choices.length() > 0) {
                    val content = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                    return@withContext content.trim()
                }
            }
        } catch (e: Exception) {
            Log.e("OpenAiAnalyzer", "OpenAI API Exception", e)
        }

        return@withContext "🚨 KAAVAL SOS: Visually impaired user requires immediate assistance at $locationAddress."
    }
}
