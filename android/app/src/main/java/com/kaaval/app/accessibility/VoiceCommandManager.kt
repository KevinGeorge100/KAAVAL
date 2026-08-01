package com.kaaval.app.accessibility

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

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
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN") // Biased for Indian English
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 20) // More results for phonetic variation
        // Helps in noisy environments by not waiting for long silence
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 800)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 400)
    }

    private var isListening = false
    private var lastShoutTime = 0L
    private val SHOUT_THRESHOLD = 8.0f // Sensitivity: 1.0 (very sensitive) to 15.0 (loud scream)

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
            if (isEmergencyTrigger(text)) {
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
            if (isEmergencyTrigger(text)) {
                Log.i("VoiceCommandManager", "CRITICAL: Partial Voice Trigger Detected: $text")
                onTriggerReceived()
                return
            }
        }
    }

    /**
     * Comprehensive Emergency Trigger Logic
     * Matches Indian English slang, phonetic variations, and Malayalam keywords.
     */
    private fun isEmergencyTrigger(text: String): Boolean {
        val lowerText = text.lowercase()
        val triggers = listOf(
            "help", "sos", "emergency", "police", "ambulance",
            "sahayam", "സഹായം", // Malayalam
            "bachao", "save me", // Indian Common
            "amma", "appa" // Phonetic cries for help in Indian context
        )
        
        return triggers.any { lowerText.contains(it) }
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
    override fun onRmsChanged(rmsdB: Float) {
        // Real-time loudness monitoring (The "Acoustic Foundation")
        // This detects shouting even if Google doesn't "understand" the word
        if (rmsdB > SHOUT_THRESHOLD) {
            val currentTime = System.currentTimeMillis()
            // Require sustained loudness or repeated shouts to avoid false triggers
            if (currentTime - lastShoutTime < 2000) {
                Log.w("VoiceCommandManager", "Acoustic Panic Detected (Loudness: $rmsdB)")
                onTriggerReceived()
            }
            lastShoutTime = currentTime
        }
    }
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
