package com.kaaval.app.core.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Text-To-Speech accessibility feedback manager for visually impaired users.
 */
class VoiceFeedbackManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.US
            isInitialized = true
        }
    }

    fun speak(text: String) {
        if (isInitialized) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "KaavalSpeech")
        }
    }

    fun speakCountdown(seconds: Int) {
        speak("$seconds")
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
