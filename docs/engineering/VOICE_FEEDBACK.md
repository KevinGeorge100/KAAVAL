# 🎙️ KAAVAL Voice Feedback Engine Architecture

**Document Version:** 1.0  
**Target Audience:** Android Engineers, Accessibility Leads, System Integrators  
**Package:** `com.kaaval.app.accessibility`  

---

## 🎯 1. System Overview

The **VoiceFeedbackManager** is a thread-safe, lifecycle-aware **Singleton Speech Synthesis Engine** for KAAVAL. It leverages Android’s `TextToSpeech` (TTS) API to provide instant, non-visual spoken status updates, guidance, and emergency alerts to visually impaired users.

### Architectural Highlights:
- **Singleton Pattern**: Built as Kotlin `object VoiceFeedbackManager`, ensuring single-instance TTS state management across activity lifecycles and background services.
- **Queued vs Priority Playback**:
  - `speak(message)` uses `TextToSpeech.QUEUE_ADD` to queue informational speech sequentially without overlapping.
  - `speakPriority(message)` uses `TextToSpeech.QUEUE_FLUSH` to immediately interrupt non-critical speech for high-priority emergency alerts.
- **Auto-Initialization**: Initialized automatically on app startup via [KaavalApplication.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/KaavalApplication.kt).
- **Multi-Language Architecture**: Built with an abstraction layer (`getMessage(englishText, malayalamText)`) supporting English by default and seamless switching to Malayalam (`ml_IN`) without refactoring.

---

## 🛠️ 2. Core API Reference

```kotlin
object VoiceFeedbackManager : TextToSpeech.OnInitListener {
    fun initialize(context: Context)
    fun shutdown()
    fun speak(message: String)
    fun stop()
    fun speakPriority(message: String)
    fun announce(announcement: AnnouncementType, isPriority: Boolean = false)
    fun setLanguage(languageCode: String)
}
```

### Announcement Types Matrix (`AnnouncementType`)

| Enum Identifier | Default Spoken Message (English) | Malayalam Spoken Message (`ml_IN`) | Playback Priority |
| :--- | :--- | :--- | :---: |
| `EMERGENCY_READY` | *"KAAVAL Emergency System is ready."* | *"കാവൽ എമർജൻസി സിസ്റ്റം തയ്യാറാണ്."* | Standard |
| `SOS_BUTTON_HELD` | *"SOS button held. Starting emergency countdown."* | *"എസ്.ഒ.എസ് ബട്ടൺ അമർത്തിപിടിച്ചിരിക്കുന്നു..."* | **Priority** |
| `EMERGENCY_COUNTDOWN_STARTED` | *"Emergency countdown started. Activating in 5 seconds. Tap cancel to stop."* | *"എമർജൻസി കൗണ്ട്ഡൗൺ ആരംഭിച്ചു..."* | **Priority** |
| `COUNTDOWN_CANCELLED` | *"Emergency countdown cancelled."* | *"എമർജൻസി കൗണ്ട്ഡൗൺ റദ്ദാക്കി."* | **Priority** |
| `EMERGENCY_ACTIVATED` | *"Emergency activated. Sending emergency alerts and sharing live GPS location."* | *"എമർജൻസി ആക്റ്റിവേറ്റായി..."* | **Priority** |
| `ACQUIRING_LOCATION` | *"Acquiring GPS location."* | *"ജി.പി.എസ് ലൊക്കേഷൻ കണ്ടെത്തുന്നു."* | Standard |
| `LOCATION_ACQUIRED` | *"GPS location acquired."* | *"ജി.പി.എസ് ലൊക്കേഷൻ കണ്ടെത്തി."* | Standard |
| `SENDING_SMS_ALERTS` | *"Sending emergency SMS alerts to caregivers."* | *"കെയർഗിവർമാർക്ക് എമർജൻസി എസ്.എം.എസ് അയക്കുന്നു."* | Standard |
| `CALLING_PRIMARY_CONTACT` | *"Initiating call to primary emergency contact."* | *"പ്രൈമറി കോൺടാക്റ്റിലേക്ക് ഫോൺ കോൾ ചെയ്യുന്നു."* | Standard |
| `LIVE_TRACKING_STARTED` | *"Live location tracking started."* | *"തത്സമയ ലൊക്കേഷൻ ട്രാക്കിംഗ് ആരംഭിച്ചു."* | Standard |
| `LIVE_TRACKING_ENDED` | *"Live location tracking session ended."* | *"തത്സമയ ലൊക്കേഷൻ ട്രാക്കിംഗ് അവസാനിപ്പിച്ചു."* | Standard |
| `EMERGENCY_COMPLETED` | *"Emergency resolved. You are marked safe."* | *"എമർജൻസി പൂർത്തിയായി. നിങ്ങൾ സുരക്ഷിതനാണ്."* | **Priority** |
| `ERROR_OBTAINING_LOCATION` | *"Warning: Unable to obtain GPS location. Emergency alerts sent without location."* | *"മുന്നറിയിപ്പ്: ജി.പി.എസ് ലൊക്കേഷൻ ലഭ്യമായില്ല."* | Standard |
| `NO_INTERNET` | *"Warning: Network connection unavailable. Using offline emergency alert dispatch."* | *"മുന്നറിയിപ്പ്: നെറ്റ്‌വർക്ക് കണക്ഷൻ ലഭ്യമല്ല."* | Standard |
| `GPS_DISABLED` | *"Warning: GPS location service is disabled. Please enable location services."* | *"മുന്നറിയിപ്പ്: ജി.പി.എസ് സർവീസ് ഓഫാണ്."* | Standard |
| `BATTERY_LOW` | *"Warning: Battery level low. Connect charger to maintain emergency tracking."* | *"മുന്നറിയിപ്പ്: ബാറ്ററി കുറവാണ്."* | Standard |

---

## 🔗 3. Application Integration Points

1. **[KaavalApplication.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/KaavalApplication.kt)**: Auto-initializes TTS engine on app process start (`VoiceFeedbackManager.initialize(this)`).
2. **[MainActivity.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/MainActivity.kt)**: Triggers priority announcements during 3-second SOS hold, countdown timer intervals, location acquisition, and emergency resolution.
3. **[MainSosScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/MainSosScreen.kt)**: Pairs non-visual speech output with tactile TalkBack semantics.
