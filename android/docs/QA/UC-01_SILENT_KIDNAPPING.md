# UC-01 — Silent Kidnapping / Abduction

## Scenario
A visually impaired college student is walking home alone through an isolated road. An attacker suddenly approaches and abducts the student. The student managers to trigger KAAVAL emergency mode briefly before the phone is snatched.

---

## Current KAAVAL Flow
1. **Activation**: User triggers SOS via Volume UP x3 or Long-press.
2. **Alerting**: System creates `EmergencySession`, starts `EmergencyForegroundService`, sends Telegram/SMS.
3. **Tracking**: `EmergencyForegroundService` initiates continuous GPS updates to Firestore.
4. **Resolution**: Safety is confirmed via "Secret Handshake" (double-tap + gesture).

---

## Activation Analysis
| Trigger | VISION | SPEECH | LOCKED | BG | STATUS | Evidence |
| :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| **Screen SOS Button** | NO | YES | FAIL | FAIL | **PASS** | `MainSosScreen.kt` |
| **Volume UP x3** | YES | YES | FAIL | FAIL | **PASS** | `MainActivity.kt#onKeyDown` |
| **Voice ("HELP")** | YES | NO | FAIL | FAIL | **PARTIAL**| `VoiceCommandManager.kt` stops in `onStop`. |
| **P-Gesture** | NO | YES | FAIL | FAIL | **PASS** | `MainSosScreen.kt` `isStrictPShape`. |
| **BLE/Wearable** | YES | YES | **PASS** | **PASS**| **PASS** | `KaavalBleManager.kt` handled by Service. |

---

## Discreetness Analysis
KAAVAL supports a **Discreet Mode** (Feature 2) which is essential for this scenario.

**Standard Mode Timeline:**
- **T+0**: Trigger. Audible: "SOS Button Held" (Standard).
- **T+1s**: Countdown. Audible: "Activating in 5... 4...".
- **T+5s**: Activated. Audible: "Emergency Activated".
- **T+10s**: Location. Audible: "Location Acquired".

**Discreet Mode Timeline:**
- **T+0**: Trigger. **SILENT**. Haptic: Double Pulse.
- **T+1s**: Countdown. **SILENT**. Haptic: Periodic Ticks.
- **T+5s**: Activated. **SILENT**. Haptic: Heavy Pulse.
- **T+30s**: Tracking. **SILENT**. Haptic: Tactile Heartbeat.

**Finding**: Discreet mode successfully suppresses most dangerous audio, but **Auto-Call behavior** (Part K) remains a risk.

---

## Lock-Screen Analysis
| Requirement | Status | Evidence |
| :--- | :--- | :--- |
| **Background Trigger** | **FAIL** | Volume keys and P-Gesture currently require `MainActivity` visibility. |
| **Lock-Screen Activation** | **FAIL** | No Accessibility Service implemented to intercept keys while locked. |
| **Service Continuation** | **PASS** | `EmergencyForegroundService` is `START_STICKY` and independent of UI. |

---

## Accessibility Analysis
The system is highly accessible for visually impaired users.
- **Vision Independence**: Physical triggers (Volume) and Haptics provide complete eyes-free operation.
- **Speech Independence**: SOS works perfectly without requiring the student to speak.

---

## Phone-Taken-Away Analysis
- **Session Persistence**: **PASS**. `EmergencySession` in Room ensures the state remains `ACTIVE` even if the app is killed.
- **Execution Ownership**: **PASS**. `EmergencyForegroundService` owns the location loop.
- **Result**: If the attacker takes the phone and kills the app, the **Foreground Service persists**, and if the phone reboots, the session is **Recovered** (Sprint 3A logic).

---

## Location Analysis
- **Continuous Tracking**: **PASS**. `EmergencyForegroundService` updates location every 30s.
- **Movement Handling**: **PASS**. Caregivers see real-time movement on the [Live Portal](https://kaaval-94c1d.web.app/live).
- **Fallbacks**: **PASS**. Fused -> Last Known -> Direct Hardware ensures location is never "waiting" for a fix.

---

## Caregiver Analysis
- **Primary Alert**: Telegram and SMS (Sprint 2 Dispatcher).
- **Live Context**: Caregiver receives a URL to a live map showing:
  - Current Latitude/Longitude.
  - Accuracy (e.g. "Within 15 meters").
  - Last updated timestamp.
- **Resolution Status**: Portal shows "COMPLETED" only when user performs gesture.

---

## Cancellation Analysis
- **Accidental Cancel**: **PASS**. Very difficult to accidentally cancel due to the **Full Screen Gesture Overlay** ('Y'/'N').
- **Forced Cancel**: **PARTIAL**. An attacker can see the "ARE YOU SAFE?" text if they look at the screen, but they won't know the gesture to confirm unless they know KAAVAL's "Secret Handshake".

---

## Safety Risks
1.  **[CRITICAL] Phone Call UI**: When SOS activates, the system initiates a phone call. This opens the **Android Phone App**, which is highly visible and produces ringing sounds. This could alert an attacker immediately. **(SAFETY RISK)**.
2.  **[MEDIUM] TalkBack Focus**: If TalkBack is on, the screen might announce "Are you safe?" aloud when the confirmation overlay appears.

---

## Test Results Summary

| ID | Requirement | Status |
| :--- | :--- | :---: |
| R1 | Silent Activation | **PASS** |
| R2 | Eyes-Free Operation | **PASS** |
| R3 | No-Voice Required | **PASS** |
| R4 | Lock-Screen Trigger | **FAIL** |
| R5 | Haptic Confirmation | **PASS** |
| R6 | Process-Death Resilience| **PASS** |
| R7 | Continuous Tracking | **PASS** |
| R8 | Caregiver Live View | **PASS** |

---

## FINAL SCORE: 🟡 PARTIALLY CAPABLE

KAAVAL is exceptionally strong once the emergency is active—the background tracking and session persistence are "Battle-Ready." However, it is **critically vulnerable during the initial 10 seconds** of an abduction due to lock-screen limitations and the visible phone call UI.

### Top 5 Risks / Gaps:
1. **No Lock-Screen Trigger**: Student cannot trigger SOS if the phone is locked in a pocket (unless using BLE wearable).
2. **Call UI Visibility**: The system dialer opening reveals the SOS to an attacker.
3. **Accessibility Service Missing**: Critical for global hardware key interception.
4. **Voice Listener Death**: Speech triggers stop working as soon as the app moves to the background.
5. **Screen Dependence**: Many triggers still require the screen to be ON.

### CRITICAL FIXES REQUIRED:
1. **Implement Accessibility Service**: To allow Volume UP x3 from the lock screen.
2. **Stealth Call Logic**: Consider delaying or making the automated call optional in Discreet Mode.
3. **Lock-Screen Visibility**: Use `showWhenLocked` flags to allow the SOS screen to appear over the lock screen.

---

**Validation Completed by KAAVAL QA Engine.** 🛡️🚀
