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
    private var currentLocale: Locale = Locale.ENGLISH
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
        BATTERY_LOW
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
                Log.e(TAG, "Language $currentLocale not supported on device")
            } else {
                isInitialized = true
                Log.d(TAG, "TextToSpeech engine initialized successfully in $currentLocale")
                
                // Flush pending messages queued before TTS completed init
                synchronized(pendingQueue) {
                    for ((msg, isPriority) in pendingQueue) {
                        speakInternal(msg, isPriority)
                    }
                    pendingQueue.clear()
                }
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
            AnnouncementType.EMERGENCY_READY -> getMessage("KAAVAL Emergency System is ready.", "കാവൽ എമർജൻസി സിസ്റ്റം തയ്യാറാണ്.")
            AnnouncementType.SOS_BUTTON_HELD -> getMessage("SOS button held. Starting emergency countdown.", "എസ്.ഒ.എസ് ബട്ടൺ അമർത്തിപിടിച്ചിരിക്കുന്നു. എമർജൻസി കൗണ്ട്ഡൗൺ ആരംഭിക്കുന്നു.")
            AnnouncementType.EMERGENCY_COUNTDOWN_STARTED -> getMessage("Emergency countdown started. Activating in 5 seconds. Tap cancel to stop.", "എമർജൻസി കൗണ്ട്ഡൗൺ ആരംഭിച്ചു. 5 സെക്കൻഡിനുള്ളിൽ ആക്റ്റിവേറ്റാകും. റദ്ദാക്കാൻ കാൻസൽ ടാപ്പ് ചെയ്യുക.")
            AnnouncementType.COUNTDOWN_CANCELLED -> getMessage("Emergency countdown cancelled.", "എമർജൻസി കൗണ്ട്ഡൗൺ റദ്ദാക്കി.")
            AnnouncementType.EMERGENCY_ACTIVATED -> getMessage("Emergency activated. Sending emergency alerts and sharing live GPS location.", "എമർജൻസി ആക്റ്റിവേറ്റായി. സന്ദേശങ്ങളും തത്സമയ ജി.പി.എസ് ലൊക്കേഷനും അയക്കുന്നു.")
            AnnouncementType.ACQUIRING_LOCATION -> getMessage("Acquiring GPS location.", "ജി.പി.എസ് ലൊക്കേഷൻ കണ്ടെത്തുന്നു.")
            AnnouncementType.LOCATION_ACQUIRED -> getMessage("GPS location acquired.", "ജി.പി.എസ് ലൊക്കേഷൻ കണ്ടെത്തി.")
            AnnouncementType.SENDING_SMS_ALERTS -> getMessage("Sending emergency SMS alerts to caregivers.", "കെയർഗിവർമാർക്ക് എമർജൻസി എസ്.എം.എസ് അയക്കുന്നു.")
            AnnouncementType.CALLING_PRIMARY_CONTACT -> getMessage("Initiating call to primary emergency contact.", "പ്രൈമറി കോൺടാക്റ്റിലേക്ക് ഫോൺ കോൾ ചെയ്യുന്നു.")
            AnnouncementType.LIVE_TRACKING_STARTED -> getMessage("Live location tracking started.", "തത്സമയ ലൊക്കേഷൻ ട്രാക്കിംഗ് ആരംഭിച്ചു.")
            AnnouncementType.LIVE_TRACKING_ENDED -> getMessage("Live location tracking session ended.", "തത്സമയ ലൊക്കേഷൻ ട്രാക്കിംഗ് അവസാനിപ്പിച്ചു.")
            AnnouncementType.EMERGENCY_COMPLETED -> getMessage("Emergency resolved. You are marked safe.", "എമർജൻസി പൂർത്തിയായി. നിങ്ങൾ സുരക്ഷിതനാണ്.")
            AnnouncementType.ERROR_OBTAINING_LOCATION -> getMessage("Warning: Unable to obtain GPS location. Emergency alerts sent without location.", "മുന്നറിയിപ്പ്: ജി.പി.എസ് ലൊക്കേഷൻ ലഭ്യമായില്ല.")
            AnnouncementType.NO_INTERNET -> getMessage("Warning: Network connection unavailable. Using offline emergency alert dispatch.", "മുന്നറിയിപ്പ്: നെറ്റ്‌വർക്ക് കണക്ഷൻ ലഭ്യമല്ല.")
            AnnouncementType.GPS_DISABLED -> getMessage("Warning: GPS location service is disabled. Please enable location services.", "മുന്നറിയിപ്പ്: ജി.പി.എസ് സർവീസ് ഓഫാണ്.")
            AnnouncementType.BATTERY_LOW -> getMessage("Warning: Battery level low. Connect charger to maintain emergency tracking.", "മുന്നറിയിപ്പ്: ബാറ്ററി കുറവാണ്.")
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
        currentLocale = if (languageCode.equals("ml", ignoreCase = true)) {
            Locale("ml", "IN")
        } else {
            Locale.ENGLISH
        }
        if (isInitialized) {
            val result = tts?.setLanguage(currentLocale)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.w(TAG, "Locale $currentLocale missing TTS voice pack on device.")
            }
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
