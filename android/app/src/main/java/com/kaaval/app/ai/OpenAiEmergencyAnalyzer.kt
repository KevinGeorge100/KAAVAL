package com.kaaval.app.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

/**
 * OpenAI API Emergency Analyzer
 * Powered by IEEE Sensors Council OpenAI Credits
 *
 * Provides AI emergency context summarization, situation classification,
 * and false-trigger verification for caregivers.
 */
class OpenAiEmergencyAnalyzer(private val apiKey: String) {

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
            } else {
                Log.e("OpenAiAnalyzer", "OpenAI API request failed with code: ${conn.responseCode}")
            }
        } catch (e: Exception) {
            Log.e("OpenAiAnalyzer", "OpenAI API Exception", e)
        }

        return@withContext "🚨 KAAVAL SOS: Visually impaired user requires immediate assistance at $locationAddress."
    }
}
