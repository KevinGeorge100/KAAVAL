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
 * OpenAI API Emergency Analyzer
 * Powered by IEEE Sensors Council OpenAI Credits
 *
 * Provides AI emergency context summarization, situational analysis via Whisper,
 * and false-trigger verification for caregivers.
 */
class OpenAiEmergencyAnalyzer(private val apiKey: String) {

    suspend fun analyzeEmergencyAudio(audioFile: File): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext "AI analysis unavailable: API Key not configured."
        if (!audioFile.exists()) return@withContext "Audio Witness file missing. Check device storage."

        try {
            // 1. Transcribe Audio using Whisper API
            val transcript = transcribeAudio(audioFile)
            
            // 2. Handle Silence or Noise (Whisper returns empty or very short strings for non-speech)
            if (transcript.trim().length < 3) {
                return@withContext "🔇 *ENVIRONMENTAL STATUS*: No clear speech detected. The user's surroundings appear to be quiet or the device is muffled. Live tracking is still active."
            }

            // 3. Analyze Situation using GPT-4o-mini
            return@withContext situationAwareness(transcript)

        } catch (e: Exception) {
            Log.e("OpenAiAnalyzer", "Full Audio Analysis Failed", e)
            return@withContext "⚠️ *AI ANALYSIS ERROR*: Technical issue during audio processing. Please check the Live Tracking URL immediately for status."
        }
    }

    private suspend fun transcribeAudio(file: File): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/audio/transcriptions")
        val boundary = "Boundary-${System.currentTimeMillis()}"
        val conn = url.openConnection() as HttpURLConnection
        
        conn.requestMethod = "POST"
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
        conn.doOutput = true

        conn.outputStream.use { out ->
            val writer = out.writer()
            writer.write("--$boundary\r\n")
            writer.write("Content-Disposition: form-data; name=\"file\"; filename=\"${file.name}\"\r\n")
            writer.write("Content-Type: audio/mpeg\r\n\r\n")
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
        }
        ""
    }

    private suspend fun situationAwareness(transcript: String): String = withContext(Dispatchers.IO) {
        val url = URL("https://api.openai.com/v1/chat/completions")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $apiKey")
        conn.doOutput = true

        val jsonBody = JSONObject().apply {
            put("model", "gpt-4o-mini")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "You are KAAVAL AI, an emergency dispatcher. Analyze this emergency audio transcript from a visually impaired user. Summarize the situation in 15 words or less. Focus on immediate danger and context.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "Transcript: $transcript")
                })
            })
            put("max_tokens", 60)
        }

        OutputStreamWriter(conn.outputStream).use { it.write(jsonBody.toString()) }

        if (conn.responseCode == 200) {
            val response = conn.inputStream.bufferedReader().use { it.readText() }
            val content = JSONObject(response).getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
            return@withContext content.trim()
        }
        "Transcript: $transcript"
    }

    suspend fun generateEmergencySummary(
        locationAddress: String,
        userMedicalProfile: String
    ): String = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext "Standard Emergency Alert: Location: $locationAddress"
        }

        try {
            val url = URL("https://api.openai.com/v1/chat/completions")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $apiKey")
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
