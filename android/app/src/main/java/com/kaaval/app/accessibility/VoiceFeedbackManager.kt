package com.kaaval.app.accessibility

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import java.util.Locale

/**
 * KAAVAL Voice Feedback Manager (Singleton)
 * Lifecycle-safe, thread-safe speech synthesis engine built on Android TextToSpeech API.
 * Provides queued audio announcements, priority emergency interruptions, and multi-language support (English baseline, Malayalam ready).
 */
object VoiceFeedbackManager : TextToSpeech.OnInitListener {

    private const val TAG = "VoiceFeedbackManager"

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var currentLocale: Locale = Locale("en", "IN") // Default to Indian English
    private val pendingQueue = mutableListOf<Pair<String, Boolean>>() // Pair(message, isPriority)

    enum class AnnouncementType {
        EMERGENCY_READY,
        SOS_BUTTON_HELD,
        EMERGENCY_COUNTDOWN_STARTED,
        COUNTDOWN_CANCELLED,
        EMERGENCY_ACTIVATED,
        ACQUIRING_LOCATION,
        LOCATION_ACQUIRED,
        SENDING_SMS_ALERTS,
        CALLING_PRIMARY_CONTACT,
        LIVE_TRACKING_STARTED,
        LIVE_TRACKING_ENDED,
        EMERGENCY_COMPLETED,
        ERROR_OBTAINING_LOCATION,
        NO_INTERNET,
        GPS_DISABLED,
        BATTERY_LOW,
        CAREGIVER_ON_THE_WAY,
        SIREN_BEACON
    }

    /**
     * Initializes the TextToSpeech engine automatically on app startup or context binding.
     */
    @Synchronized
    fun initialize(context: Context) {
        if (tts == null) {
            Log.d(TAG, "Initializing TextToSpeech Singleton engine...")
            tts = TextToSpeech(context.applicationContext, this)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(currentLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale $currentLocale missing. Falling back to standard English.")
                tts?.setLanguage(Locale.US)
                currentLocale = Locale.US
            }
            
            // Adjust pitch and rate for better "Indian Emergency" clarity
            tts?.setPitch(1.1f) // Slightly higher pitch for authority
            tts?.setSpeechRate(0.95f) // Slightly slower for clear instruction in noise
            
            isInitialized = true
            Log.d(TAG, "TextToSpeech engine initialized successfully in $currentLocale")
            
            // Flush pending messages queued before TTS completed init
            synchronized(pendingQueue) {
                for ((msg, isPriority) in pendingQueue) {
                    speakInternal(msg, isPriority)
                }
                pendingQueue.clear()
            }
        } else {
            Log.e(TAG, "TextToSpeech initialization failed with status $status")
        }
    }

    /**
     * Queues a spoken message. Queues speech (TextToSpeech.QUEUE_ADD) to prevent overlapping audio.
     */
    fun speak(message: String) {
        speakInternal(message, isPriority = false)
    }

    /**
     * Priority speech interruption (TextToSpeech.QUEUE_FLUSH) for critical emergency alerts.
     */
    fun speakPriority(message: String) {
        speakInternal(message, isPriority = true)
    }

    /**
     * Stops current speech playback and clears pending speech queue.
     */
    fun stop() {
        if (isInitialized) {
            tts?.stop()
        }
        synchronized(pendingQueue) {
            pendingQueue.clear()
        }
    }

    /**
     * Shuts down TextToSpeech engine cleanly and releases device resources.
     */
    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
        Log.d(TAG, "TextToSpeech engine shut down")
    }

    /**
     * Provides standardized spoken announcements for pre-defined KAAVAL emergency events.
     */
    fun announce(announcement: AnnouncementType, isPriority: Boolean = false) {
        val text = when (announcement) {
            AnnouncementType.EMERGENCY_READY -> getMessage("KAAVAL system is active. Your family is protected.", "കാവൽ സിസ്റ്റം സജ്ജമാണ്. നിങ്ങൾ സുരക്ഷിതനാണ്.")
            AnnouncementType.SOS_BUTTON_HELD -> getMessage("SOS alert initiated. Alerting family now.", "അപകട സന്ദേശം അയക്കുന്നു. വീട്ടുകാരെ വിവരമറിയിക്കുന്നു.")
            AnnouncementType.EMERGENCY_COUNTDOWN_STARTED -> getMessage("Alerting in", "സന്ദേശം അയക്കാൻ")
            AnnouncementType.COUNTDOWN_CANCELLED -> getMessage("Alert cancelled.", "സന്ദേശം റദ്ദാക്കി.")
            AnnouncementType.EMERGENCY_ACTIVATED -> getMessage("Emergency alert sent. Location shared with caregivers.", "അപകട സന്ദേശം അയച്ചു. ലൊക്കേഷൻ വീട്ടുകാർക്ക് കൈമാറി.")
            AnnouncementType.ACQUIRING_LOCATION -> getMessage("Finding your location.", "നിങ്ങളുടെ സ്ഥലം കണ്ടെത്തുന്നു.")
            AnnouncementType.LOCATION_ACQUIRED -> getMessage("Location fixed.", "സ്ഥലം കണ്ടെത്തി.")
            AnnouncementType.SENDING_SMS_ALERTS -> getMessage("Sending urgent SMS to your contacts.", "വീട്ടുകാർക്ക് അടിയന്തര സന്ദേശങ്ങൾ അയക്കുന്നു.")
            AnnouncementType.CALLING_PRIMARY_CONTACT -> getMessage("Connecting call to your primary contact.", "പ്രൈമറി കോൺടാക്റ്റിലേക്ക് വിളിക്കുന്നു.")
            AnnouncementType.LIVE_TRACKING_STARTED -> getMessage("Tracking is now live.", "തത്സമയ വിവരങ്ങൾ കൈമാറുന്നു.")
            AnnouncementType.LIVE_TRACKING_ENDED -> getMessage("Tracking stopped.", "വിവരങ്ങൾ കൈമാറുന്നത് അവസാനിപ്പിച്ചു.")
            AnnouncementType.EMERGENCY_COMPLETED -> getMessage("Emergency resolved. You are marked safe.", "നിങ്ങൾ സുരക്ഷിതനാണെന്ന് രേഖപ്പെടുത്തി.")
            AnnouncementType.ERROR_OBTAINING_LOCATION -> getMessage("GPS warning. Finding location manually.", "ജി.പി.എസ് തകരാർ. സ്ഥലം കണ്ടെത്താൻ ശ്രമിക്കുന്നു.")
            AnnouncementType.NO_INTERNET -> getMessage("Network unavailable. Using offline SMS mode.", "നെറ്റ്‌വർക്ക് ലഭ്യമല്ല. ഓഫ്‌ലൈൻ മോഡ് ഉപയോഗിക്കുന്നു.")
            AnnouncementType.GPS_DISABLED -> getMessage("Please switch on GPS for safety tracking.", "ദയവായി ജി.പി.എസ് ഓൺ ചെയ്യുക.")
            AnnouncementType.BATTERY_LOW -> getMessage("Low battery. Please charge your phone for safety.", "ബാറ്ററി കുറവാണ്. ദയവായി ചാർജ് ചെയ്യുക.")
            AnnouncementType.CAREGIVER_ON_THE_WAY -> getMessage("Your caregiver has seen your alert. Help is coming.", "സഹായം വരുന്നു. നിങ്ങളുടെ സന്ദേശം വീട്ടുകാർ കണ്ടു.")
            AnnouncementType.SIREN_BEACON -> "🚨" // Placeholder for an actual loud audio file or high-pitched tone
        }

        if (isPriority) {
            speakPriority(text)
        } else {
            speak(text)
        }
    }

    /**
     * Supports switching language locale (e.g. "en" for English, "ml" for Malayalam).
     */
    fun setLanguage(languageCode: String) {
        val targetLocale = if (languageCode.equals("ml", ignoreCase = true)) {
            Locale("ml", "IN")
        } else {
            Locale("en", "IN")
        }
        
        if (currentLocale == targetLocale && isInitialized) return

        currentLocale = targetLocale
        if (isInitialized) {
            val result = tts?.setLanguage(targetLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale $targetLocale missing. Falling back to standard English.")
                tts?.setLanguage(Locale.US)
                currentLocale = Locale.US
            }
            Log.d(TAG, "Language switched to $currentLocale")
        }
    }

    private fun speakInternal(text: String, isPriority: Boolean) {
        if (!isInitialized) {
            synchronized(pendingQueue) {
                pendingQueue.add(Pair(text, isPriority))
            }
            return
        }

        val queueMode = if (isPriority) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
        val utteranceId = "KAAVAL_VOICE_${System.currentTimeMillis()}"
        tts?.speak(text, queueMode, null, utteranceId)
    }

    private fun getMessage(englishText: String, malayalamText: String): String {
        return if (currentLocale.language.equals("ml", ignoreCase = true)) {
            malayalamText
        } else {
            englishText
        }
    }
}
