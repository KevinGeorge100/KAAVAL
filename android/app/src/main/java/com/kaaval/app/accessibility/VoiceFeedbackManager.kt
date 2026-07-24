package com.kaaval.app.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

class VoiceFeedbackManager(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var isInitialized = false
    private var currentLanguage: Locale = Locale.ENGLISH

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(currentLanguage)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("VoiceFeedbackManager", "Language not supported")
            } else {
                isInitialized = true
                Log.d("VoiceFeedbackManager", "TTS Initialized successfully")
            }
        } else {
            Log.e("VoiceFeedbackManager", "TTS Initialization failed")
        }
    }

    fun speak(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (isInitialized) {
            tts?.speak(text, queueMode, null, "KAAVAL_VOICE_ID_${System.currentTimeMillis()}")
        }
    }

    fun speakCountdown(seconds: Int) {
        speak("Emergency activating in $seconds seconds. Tap screen to cancel.", TextToSpeech.QUEUE_FLUSH)
    }

    fun setLanguage(languageCode: String) {
        currentLanguage = if (languageCode == "ml") {
            Locale("ml", "IN")
        } else {
            Locale.ENGLISH
        }
        if (isInitialized) {
            tts?.setLanguage(currentLanguage)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
