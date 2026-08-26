# KAAVAL — ADVERSARIAL EMERGENCY SYSTEM QA AUDIT

**Audit Date**: August 10, 2026
**Role**: Senior QA Engineer / Reliability Engineer
**Objective**: Evaluate KAAVAL against 30 adversarial real-world emergency scenarios.

---

## EMERGENCY LIFECYCLE VALIDATION MATRIX

| Condition | Expected Behavior | Status |
| :--- | :--- | :--- |
| Activity destroyed | Should survive (State recovered from Room) | **PASS** |
| App backgrounded | Should survive (Foreground Service Active) | **PASS** |
| Screen locked | Should survive (Foreground Service Active) | **PASS** |
| Process killed | Needs explicit OS restart testing | **PARTIAL** |
| Phone reboot | NOT IMPLEMENTED — FUTURE RELIABILITY TASK | **FAIL** |
| Battery dead | Impossible to track | **N/A** |

---

## 30 ADVERSARIAL SCENARIOS

### TC-01 — Kidnapping / Silent Emergency
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-01 | Kidnapping / Silent Emergency | Silent trigger via Volume buttons and P-Gesture. | SOS activates without sound, no visible UI change initially, tracking starts. | Trigger is silent, but **Auto-Call initiates dialer/call screen**, potentially revealing the phone's activity to an attacker. | **PARTIAL** | `MainActivity.kt` overrides volume buttons; `SosDispatcher.kt` initiates `ACTION_CALL`. | Call ringing/UI may alert kidnapper. | Implement "True Stealth" mode where auto-call is delayed or optional. |

### TC-02 — Attacker Watching the Phone
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-02 | Attacker Watching the Phone | Physical triggers (Volume UP x3). | Trigger SOS without opening the app or looking at the screen. | Volume button trigger works only when **Activity is in foreground**. No background trigger implemented. | **FAIL** | `MainActivity.kt` handles `onKeyDown`. | User must visibly unlock/open app to trigger. | Implement a Background Trigger (Accessibility Service or Media Button receiver). |

### TC-03 — Phone Immediately Snatched
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-03 | Phone Immediately Snatched | Foreground Service for location. | Emergency session continues even if the app UI is closed or the phone is locked. | `EmergencyForegroundService` is started, but it **does not contain logic to continue GPS updates** or dispatch logic; it only shows a notification. | **FAIL** | `EmergencyForegroundService.kt` lacks a location update loop. | Emergency "dies" if the app process is restricted. | Move GPS update logic into the Foreground Service. |

### TC-04 — User Cannot Speak
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | : :--- | :--- | :--- | :--- | :--- | :--- |
| TC-04 | User Cannot Speak | Physical and gesture triggers. | SOS works via touch/physical interaction only. | Volume buttons, P-gesture, and long-press SOS button all work without voice. | **PASS** | `MainActivity.kt`, `MainSosScreen.kt`. | None. | N/A |

### TC-05 — Phone Inaccessible (Pocket/Bag)
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-05 | Phone Inaccessible | Volume buttons & BLE Wearable. | Trigger SOS through clothing or via a remote wearable button. | Volume buttons work (if app in foreground). BLE scanning exists in `KaavalBleManager.kt`. | **PARTIAL** | `MainActivity.kt`, `KaavalBleManager.kt`. | Reliability of BLE in background is unverified. | **HARDWARE TEST REQUIRED**. Implement Background Volume trigger. |

### TC-06 — User Being Followed
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-06 | User Being Followed | Full SOS only. | A "Pre-SOS" or "Discreet Check-in" to notify caregivers without a full alarm. | Only full SOS triggers (Countdown -> SMS -> Call) are implemented. | **FAIL** | `EmergencyStateManager.kt` logic. | Full alarm might escalate the situation. | Implement "Yellow Alert" (Discreet check-in). |

### TC-07 — Public Transport Harassment
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-07 | Public Transport Harassment | Physical volume button trigger. | Discreet activation in a crowd. | 3x Volume UP works. Silent mode (Feature 2) silences voice feedback. | **PASS** | Feature 2 implementation in `MainActivity.kt`. | None. | N/A |

### TC-08 — Medical Emergency While Alone
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-08 | Medical Emergency While Alone | Voice triggers ("Help"). | Minimal interaction to trigger alert. | Voice triggers work if app is open. Long-press/Physical triggers available. | **PASS** | `VoiceCommandManager.kt`. | Microphone sensitivity in a medical crisis (weak voice). | Implement "Impact Detection" or "Dead Man Switch". |

### TC-09 — Fall / Phone Out of Reach
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-09 | Fall / Phone Out of Reach | Voice Command / BLE Wearable. | Trigger via voice shout or wearable. | Voice listener works in foreground. BLE logic exists. | **PARTIAL** | `VoiceCommandManager.kt`, `KaavalBleManager.kt`. | Voice listener likely dies in background. | Implement background Voice/BLE listener. |

### TC-10 — User Unconscious
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | : :--- | :--- | :--- |
| TC-10 | User Unconscious | None. | Automatic trigger on impact or prolonged inactivity. | No automatic trigger mechanism implemented. | **NOT IMPLEMENTED** | `EmergencyStateManager.kt` events. | No protection for sudden unconsciousness. | Implement Accelerometer-based Fall Detection. |

### TC-11 — Very Low Battery
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-11 | Very Low Battery | Battery Guardian. | Dispatch "Final SOS" with location at 5% battery. | `BatteryGuardianManager` triggers `CriticalBatteryDetected` event which sends a final SMS. | **PASS** | `BatteryGuardianManager.kt`, `EmergencyStateManager.kt`. | None. | PHYSICAL DEVICE TEST REQUIRED. |

### TC-12 — No Internet
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-12 | No Internet | SMS & Call fallback. | System uses cellular channels if data is down. | `SosDispatcher.kt` sends SMS and initiates Voice Call after Telegram (Internet) attempt. | **PASS** | `SosDispatcher.kt`. | None. | NETWORK TEST REQUIRED. |

### TC-13 — GPS Delayed
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-13 | GPS Delayed | 10s Timeout + Fallback. | System does not hang while waiting for GPS lock. | `LocationManager.kt` uses `withTimeoutOrNull(10.seconds)` then falls back to Last Known. | **PASS** | `LocationManager.kt`. | None. | N/A |

### TC-14 — Last Known Location
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-14 | Last Known Location | Multi-stage fallback. | Use stale location if fresh GPS fails. | `LocationManager.kt` falls back to `fusedLocationClient.lastLocation` and then `AndroidLocationManager`. | **PASS** | `LocationManager.kt`. | Stale location might be inaccurate. | Communicate "Accuracy" or "Age" of location in the SMS. |

### TC-15 — Primary Caregiver Doesn't Answer
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-15 | Primary Caregiver Doesn't Answer | Call Primary only. | System cycles through contacts until someone answers. | Logic only calls the `primaryContact` once. No retry or cycling implemented. | **FAIL** | `SmsActionHandler.kt#handleCall`. | Single point of failure if primary contact is unavailable. | Implement "Caregiver Cycling" (Call Contact 1 -> No answer -> Call Contact 2). |

### TC-16 — Caregiver Phone Off
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-16 | Caregiver Phone Off | SMS to all contacts. | Multiple channels (SMS, Telegram) to multiple people. | `SosDispatcher.kt` loops through **all** contacts for SMS. | **PASS** | `SosDispatcher.kt`. | None. | NETWORK TEST REQUIRED. |

### TC-17 — Caregiver Receives Poor Alert
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-17 | Caregiver Receives Poor Alert | Context-rich alerts. | Alert includes Name, Location, Medical Info, and Live Link. | Alerts include all 4 required fields. | **PASS** | `SosDispatcher.kt` alert text templates. | Medical info might be too long for 1 SMS. | N/A |

### TC-18 — False Activation
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-18 | False Activation | 3s Hold + 5s Countdown. | Opportunity to cancel before alerts go out. | User has 8 seconds total to cancel. Haptic "ticking" confirms countdown. | **PASS** | `EmergencyStateManager.kt`. | None. | N/A |

### TC-19 — User Cannot Hear Voice Feedback
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-19 | User Cannot Hear Voice Feedback | Haptic Feedback manager. | Critical status conveyed via touch. | Rhythmic haptics for hold, countdown, and confirmation are implemented. | **PASS** | `HapticFeedbackManager.kt`. | None. | N/A |

### TC-20 — Classroom / Discreet Mode
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-20 | Classroom / Discreet Mode | Silent operation. | No loud announcements during trigger. | Feature 2 (Discreet Mode) silences intermediate announcements. | **PASS** | `EmergencyActionDispatcher.kt` filter logic. | None. | N/A |

### TC-21 — Hospital Environment
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-21 | Hospital Environment | Medical Profile Access. | First responders can see medical info. | `MedicalProfileScreen.kt` displays data. | **PASS** | `MedicalProfileScreen.kt`. | Data is locked behind phone PIN if app isn't open. | Implement "Lock Screen Medical Card" or Notification. |

### TC-22 — Domestic Threat
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-22 | Domestic Threat | Silent Emergency. | Hide SOS activity from someone nearby. | "Blackout UI" hides the screen. | **PASS** | `MainSosScreen.kt` Stealth UI. | Phone call is still visible/audible. | N/A |

### TC-23 — User Being Chased
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-23 | User Being Chased | Live Tracking. | Continuous location updates. | App has a `LiveTracking` state, but **no logic to periodically send new coordinates** to caregivers. It only sends location ONCE at start. | **FAIL** | `SosDispatcher.kt` and `EmergencyStateManager.kt`. | Caregiver only sees the *start* point, not where the user is now. | Implement a `WorkManager` or Service-based periodic location pusher. |

### TC-24 — Network Lost During Tracking
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-24 | Network Lost During Tracking | None. | Queue location updates until network returns. | No offline queuing or retry logic for Telegram/Data alerts. | **NOT IMPLEMENTED** | `SosDispatcher.kt`. | Permanent loss of update if network blips. | Implement persistence for failed alerts. |

### TC-25 — Phone Reboots During Emergency
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-25 | Phone Reboots During Emergency | Persistence (Room). | Emergency resumes after reboot. | State is saved to `emergency_current_state` table, but **no BOOT_COMPLETED receiver** exists to restart the foreground service. | **PARTIAL** | `EmergencyStateEntity.kt`, `AndroidManifest.xml`. | Emergency tracking stops until user manually opens app. | Implement `BootReceiver` to restart SOS session. |

### TC-26 — App Process Killed
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-26 | App Process Killed | Foreground Service. | Tracking survives system memory pressure. | `EmergencyForegroundService` is `START_STICKY`. | **PASS** | `EmergencyForegroundService.kt`. | Notification alone isn't enough; needs logic inside service. | Move location logic into Service. |

### TC-27 — BLE Wearable Disconnects
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-27 | BLE Wearable Disconnects | Internal state management. | SOS continues on phone if wearable fails. | Phone state machine is independent of BLE connection once triggered. | **PASS** | `EmergencyStateManager.kt`. | None. | N/A |

### TC-28 — Wearable Battery Dies
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-28 | Wearable Battery Dies | Independent triggers. | Phone triggers (Volume, P-Gesture) remain available. | Triggers are multi-modal. | **PASS** | `MainActivity.kt`. | None. | N/A |

### TC-29 — Accidental "I AM SAFE"
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-29 | Accidental "I AM SAFE" | Gesture Confirmation (Y/N). | High friction for safety resolution. | Double-tap + complex shape ('V') required. | **PASS** | `MainSosScreen.kt` gesture recognition. | None. | N/A |

### TC-30 — Forced Cancellation
| ID | Scenario | Current Capability | Expected | Actual | Status | Evidence | Risk | Required Change |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| TC-30 | Forced Cancellation | Secret handshakes. | User can't be easily forced to stop the SOS. | "Secret Handshake" hides the drawing method, but no "Duress Code" exists. | **PARTIAL** | `MainSosScreen.kt`. | Attacker might eventually figure out the stop gesture. | Implement "Duress Safe" (A gesture that *looks* like safe but sends a silent "I am being forced" alert). |

---

## ARCHITECTURE AUDIT

### 1. God Objects
*   **`MainActivity`**: Even after refactor, it initializes 11+ separate managers. If `MainActivity` is destroyed by the system, these managers (and their listeners) may become inconsistent.
*   **Recommendation**: Move infrastructure initialization to a `DependencyProvider` or use a DI framework (Hilt).

### 2. Lifecycle Problems
*   **`KaavalBleManager`**: Scans are started in `onCreate` but not explicitly stopped in `onStop`/`onDestroy` if not connected, potentially draining battery.
*   **`SmsReplyReceiver`**: Dynamically registered in `MainActivity`. If the Activity is killed (even if Service is running), the app stops listening for caregiver replies.

### 3. Coroutine Leaks
*   **`LocationActionHandler`**: Uses a global `CoroutineScope(Dispatchers.Main)`. This scope is never cancelled. If multiple location requests are made, multiple jobs will run indefinitely.
*   **`SmsActionHandler`**: Uses `CoroutineScope(Dispatchers.IO)` similarly.

### 4. Race Conditions
*   **State Recovery**: `MainActivity` init block collects from `currentEmergencyState`. If the DB update from a previous session happens at the same time as initialization, the state might flip-flop.

### 5. Duplicate Emergency Sessions
*   **Trigger Overlap**: If a user shakes the phone (Event 1) and then presses the Volume buttons (Event 2) during the countdown, the `EmergencyStateManager` handles it, but the `MainActivity` might trigger `startCountdown()` logic twice if not careful (though current logic uses `InstantSosTriggered` which is safer).

### 6. Background Execution Risks
*   **CRITICAL**: Android 14+ severely restricts background starts. `SosDispatcher` uses `ACTION_CALL`, which is often blocked from the background.
*   **GPS**: The `EmergencyForegroundService` does not actually request location updates. It just "exists" to keep the process alive.

### 7. Accessibility Failures
*   **Dialog Focus**: When the "Are you safe?" dialog appears, TalkBack focus might get trapped inside the dialog, making the drawing gestures on the *overlay behind it* difficult to perform.

### 8. Single Points of Failure
*   **Telegram Bot**: If the Telegram API is rate-limited or the token is invalid, there is no automatic retry or error reporting to the user.

---

## AUDIT SUMMARY

### A. Critical Failures
1.  **Continuous Tracking Missing**: Location is only sent once at the start of the SOS. A user being moved (kidnapped/chased) will not be tracked.
2.  **Background Interaction Blocked**: Physical volume triggers and gestures only work when the app is visibly open on screen.
3.  **Foreground Service Empty**: The service does not actually perform GPS updates, making it a "ghost service" that may be killed by the OS.

### B. High-Priority Failures
1.  **Single Caregiver Call**: The system stops after calling the primary contact. It does not cycle to the next contact if there's no answer.
2.  **Lock Screen Inaccessibility**: The app is completely unreachable if the phone is locked.

### C. Medium-Priority Issues
1.  **Boot Survivability**: No mechanism to restart tracking after a phone reboot.
2.  **Coroutine Leaks**: Handlers in the domain layer are leaking memory.

### D. Low-Priority Issues
1.  **BLE Cleanup**: Scan should be more lifecycle-aware.
2.  **Medical Card**: Needs a persistent notification for first responders.

### E. Features that are already strong
1.  **State Machine**: The `EmergencyStateManager` is robust and handles transitions correctly.
2.  **Safety Net**: The automated test suite is a strong foundation.
3.  **Haptic feedback**: Very well implemented for the target demographic.

### F. Features that are only simulated
1.  **Analytics**: `AnalyticsActionHandler` is just a log.
2.  **Audio Analysis**: OpenAI integration was rolled back to a local-only recording.

### G. Features completely missing
1.  **Fall Detection** (Accelerometer).
2.  **Offline Location Queuing**.
3.  **Lock-screen Overlay**.

### H. Recommended Implementation Order
1.  **Background GPS updates** (Move logic into Foreground Service).
2.  **Background Physical Triggers** (Implement Accessibility Service for Volume buttons).
3.  **Continuous Location Pusher** (Periodic Telegram/SMS updates).
4.  **Caregiver Cycling** (Iterate through contacts list on call failure).
5.  **Lock-screen Visibility** (Show on top of lock screen).
