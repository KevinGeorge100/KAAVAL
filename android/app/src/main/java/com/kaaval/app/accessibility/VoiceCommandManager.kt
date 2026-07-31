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
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10)
        // Helps in noisy environments by not waiting for long silence
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1000)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 500)
    }

    private var isListening = false

    fun startListening() {
        if (isListening) return
        
        Log.d("VoiceCommandManager", "Initializing SpeechRecognizer...")
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@VoiceCommandManager)
            }
        }
        
        try {
            speechRecognizer?.startListening(recognizerIntent)
            isListening = true
            Log.i("VoiceCommandManager", "VOICE SYSTEM ACTIVE: Listening for 'HELP' or 'SOS'...")
        } catch (e: Exception) {
            Log.e("VoiceCommandManager", "Critical error starting speech recognizer", e)
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
            // Check for English and Malayalam triggers
            if (lowerText.contains("help") || 
                lowerText.contains("sos") || 
                lowerText.contains("emergency") || 
                lowerText.contains("സഹായം") || 
                lowerText.contains("sahayam")) {
                
                Log.i("VoiceCommandManager", "CRITICAL: Partial Voice Trigger Detected: $text")
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
        
        // Restart on common non-fatal errors or if busy
        if (isListening) {
            // If busy, wait a moment before retrying
            val delay = if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) 500L else 100L
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (isListening) speechRecognizer?.startListening(recognizerIntent)
            }, delay)
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
