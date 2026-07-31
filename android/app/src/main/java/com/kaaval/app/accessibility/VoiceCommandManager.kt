package com.kaaval.app.accessibility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import java.util.Locale

/**
 * KAAVAL Voice Command Manager
 * Uses Android SpeechRecognizer to listen for emergency triggers ("HELP", "SOS").
 * Designed for low-power background listening when the app is in the foreground.
 */
class VoiceCommandManager(
    private val context: Context,
    private val onTriggerReceived: () -> Unit
) : RecognitionListener {

    private var speechRecognizer: SpeechRecognizer? = null
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    private var isListening = false

    fun startListening() {
        if (isListening) return
        
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceCommandManager)
            }
        }
        
        try {
            speechRecognizer?.startListening(recognizerIntent)
            isListening = true
            Log.d("VoiceCommandManager", "Started listening for emergency commands...")
        } catch (e: Exception) {
            Log.e("VoiceCommandManager", "Error starting speech recognizer", e)
        }
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { text ->
            val lowerText = text.lowercase()
            if (lowerText.contains("help") || lowerText.contains("sos") || lowerText.contains("emergency")) {
                Log.i("VoiceCommandManager", "Voice Trigger Detected: $text")
                onTriggerReceived()
                return
            }
        }
        // Restart listening to maintain "always on" feel while app is open
        if (isListening) {
            speechRecognizer?.startListening(recognizerIntent)
        }
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        matches?.forEach { text ->
            val lowerText = text.lowercase()
            if (lowerText.contains("help") || lowerText.contains("sos") || lowerText.contains("emergency")) {
                Log.i("VoiceCommandManager", "Partial Voice Trigger Detected: $text")
                onTriggerReceived()
                return
            }
        }
    }

    override fun onError(error: Int) {
        val message = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
            SpeechRecognizer.ERROR_SERVER -> "Server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
        Log.e("VoiceCommandManager", "Speech recognition error: $message")
        
        // Restart on common non-fatal errors
        if (isListening && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
            speechRecognizer?.startListening(recognizerIntent)
        }
    }

    // Unused listener methods
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
