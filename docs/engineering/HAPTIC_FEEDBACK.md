# 📳 KAAVAL Haptic Feedback Engine Specification

**Document Version:** 1.0  
**Target Audience:** Android Engineers, Accessibility Auditors, Embedded Hardware Team  
**Package:** `com.kaaval.app.accessibility`  

---

## 🎯 1. Overview & Architecture

The **HapticFeedbackManager** is a thread-safe, lifecycle-safe **Singleton Haptic Engine** for KAAVAL. It delivers immediate tactile feedback to visually impaired users during high-stress emergency situations, complementing VoiceFeedbackManager (Text-to-Speech) and TalkBack screen reader support.

### Architectural Highlights:
- **Singleton Pattern**: Implemented as Kotlin `object HapticFeedbackManager`, preventing redundant `Vibrator` service allocations and system memory leaks.
- **Android Compatibility (API 26-34+)**:
  - Uses `VibratorManager` on Android 12+ (`Build.VERSION_CODES.S` / API 31+).
  - Gracefully falls back to `Context.VIBRATOR_SERVICE` on API 26–30.
  - Automatically verifies `hasVibrator()` before triggering patterns to prevent crashes on non-vibrating devices or restricted emulator environments.
- **Non-Blocking Execution**: Haptic pattern execution runs off the main looper thread using hardware waveform APIs (`VibrationEffect.createWaveform` / `createOneShot`), keeping UI rendering completely smooth.

---

## 📳 2. Tactile Waveform Specification Matrix (`HapticPattern`)

| Event Identifier (`HapticPattern`) | Waveform Pattern Description | Waveform Timings (ms) | Amplitude Modulation | Rationale / Use Case |
| :--- | :--- | :--- | :--- | :--- |
| `SOS_HOLD` | Short tactile pulse | `[0, 50]` | `[0, 255]` | Instant physical confirmation when user presses tactile SOS control. |
| `COUNTDOWN_TICK` | Very short pulse per second | `[0, 35]` | `[0, 255]` | Metronome pulse indicating active 5s cancellation window. |
| `COUNTDOWN_CANCELLED` | Descending double pulse | `[0, 100, 60, 150]` | `[0, 255, 0, 100]` | Distinct rumble confirming emergency cancellation. |
| `SOS_ACTIVATED` | Long strong SOS sequence | `[0, 100, 100, 100, ...]` | `[0, 255, 0, 255, ...]` | Powerful multi-pulse pattern confirming full SOS alert dispatch. |
| `SMS_SENT` | Double confirmation pulse | `[0, 80, 60, 80]` | `[0, 200, 0, 255]` | Tactile alert when emergency SMS dispatch completes. |
| `CALL_STARTED` | Medium steady pulse | `[0, 300]` | `[0, 255]` | Vibrational prompt when auto-dialing primary emergency contact. |
| `LOCATION_ACQUIRED` | Triple short pulse | `[0, 60, 40, 60, 40, 60]` | `[0, 180, 0, 220, 0, 255]` | Ascending triple pulse confirming GPS location fix. |
| `LIVE_TRACKING_STARTED` | Long-short-long pattern | `[0, 350, 100, 100, 100, 350]` | `[0, 255, 0, 150, 0, 255]` | Tactile indicator that live caregiver tracking link is active. |
| `SUCCESS` | Two pleasant pulses | `[0, 100, 80, 150]` | `[0, 150, 0, 255]` | Confirmation when user resolves emergency ("I AM SAFE NOW"). |
| `ERROR` | Rapid repeated pulses | `[0, 50, 50, 50, 50, 50, 50, 50]` | `[0, 255, 0, 255, 0, 255, 0, 255]` | Warning vibration for location failures or dispatch errors. |
| `LOW_BATTERY` | Slow double pulse | `[0, 200, 250, 200]` | `[0, 120, 0, 120]` | Gentle double pulse warning user of low phone battery. |
| `NO_INTERNET` | Pause then pulse | `[0, 400, 100, 150]` | `[0, 100, 0, 255]` | Warning pattern when offline mode is engaged. |
| `GPS_DISABLED` | Two long pulses | `[0, 450, 200, 450]` | `[0, 220, 0, 220]` | Warning pattern if location services are disabled. |

---

## ♿ 3. Accessibility Rationale

- **Multimodal Feedback**: Visually impaired users in high-stress situations may be in noisy environments (where voice TTS is hard to hear) or quiet environments (where audio output could escalate danger). Tactile feedback provides silent, unambiguous status feedback directly through the device casing.
- **Complementarity**: Haptic patterns do not replace `VoiceFeedbackManager` or TalkBack screen readers; rather, they fire in parallel to reinforce non-visual cues.

---

## ⌚ 4. Future BLE Wearable Hardware Integration

The `HapticPattern` waveform timings and amplitude matrices are designed to be serialized over Bluetooth Low Energy (BLE) to the KAAVAL BLE Wearable device (ESP32-C3 / nRF52840). When the physical wearable button is triggered or receives an alert, the same tactile pulse pattern will be executed on the wearable's ERM/LRA vibration motor.
