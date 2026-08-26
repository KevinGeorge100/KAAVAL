# KAAVAL
![KAAVAL Emergency Response Ecosystem](banner.png)

**Accessibility-First Emergency Response Ecosystem for Visually Impaired Individuals**

Supported by the **IEEE Sensors Council Industry Mentoring Program**.

---

## 👁️ Vision
> **Our goal is not to build another emergency app.**
> 
> We are building an **accessibility-first emergency response ecosystem** that enables visually impaired individuals to request help instantly and ensures that caregivers coordinate an effective response until the user is safe.

---

## 🚨 Problem Statement
Visually impaired individuals face three critical challenges during emergencies because existing systems assume users can quickly locate, unlock, and operate a smartphone:

1.  **Emergency Activation**: Locating or operating a digital screen during a high-stress crisis is nearly impossible without sight.
2.  **Response Assurance**: After triggering an SOS, users often have zero feedback on whether help is actually coming, leading to extreme anxiety.
3.  **Caregiver Coordination**: Multiple family members may receive alerts simultaneously, but without coordination, response is often delayed or redundant.

---

## 🛡️ Our Solution: The SAHAAYA Ecosystem
KAAVAL (Sahaaya) manages the **complete emergency response workflow** — from instant activation to caregiver acknowledgement and incident closure.

> **The Differentiator**: While standard systems send a message, **KAAVAL coordinates the response.**

### Ecosystem Components:
- ⌚ **Tactile Wearable**: A Bluetooth-enabled locket with a dedicated SOS button and haptic feedback.
- 📱 **User Application**: A voice-first, high-contrast Android app optimized for TalkBack and gesture control.
- 👨‍👩‍👧 **Caregiver Network**: A coordination platform for family, NGOs, and mentors to acknowledge alerts and track live ETA.
- ☁️ **Cloud Coordination**: A real-time engine managing incident state and caregiver routing.

---

## 👥 Target Users

### Primary Users
*   **Visually Impaired Individuals**: Students and adults who require a reliable, non-visual way to call for help.

### Secondary Users (Responders)
*   **Family & Caregivers**: Immediate emergency contacts.
*   **Educational Institutions**: Schools and NGOs for the visually impaired.
*   **Security Teams**: Campus security or local first responders.

---

## 🌐 Live Web Ecosystem Deployment
- 📱 **Live Interactive Web Simulator**: [https://kaaval-94c1d.web.app](https://kaaval-94c1d.web.app)
- 📡 **Caregiver Live Location Portal**: [https://kaaval-94c1d.web.app/live](https://kaaval-94c1d.web.app/live)

---

## 📈 Completed Sprint Milestones

### ✅ Sprint 1 — Project Foundation (`M1`)
- Repository structure, Android Compose shell, Room DB local persistence, Firebase setup, and local web simulator.

### ✅ Sprint 2 — Accessibility Infrastructure & Engines
- ✅ **Task 2.1 — High-Contrast Accessibility Theme**: Material 3 Pure Black (`#000000`) & KAAVAL Yellow (`#FFD600`) design system.
- ✅ **Task 2.2 — TalkBack Accessibility**: Full Jetpack Compose Semantics and explicit descriptions.
- ✅ **Task 2.3 — Voice Feedback Engine**: `VoiceFeedbackManager` Singleton engine, TextToSpeech integration.
- ✅ **Task 2.4 — Haptic Feedback Engine**: `HapticFeedbackManager` with 14 `HapticPattern` waveforms.

### ✅ Sprint 3 — Resilience & Live Tracking
- ✅ **Task 3.1 — Persistent Emergency Sessions**: SOS state is now stored in Room DB, ensuring recovery after app crashes or phone restarts.
- ✅ **Task 3.2 — Real-Time Location Engine**: 3-layer GPS fallback (Fresh lock -> Last known -> Direct hardware) with continuous 30s background updates.
- ✅ **Task 3.3 — Secure Live Tracking**: Integrated Firebase Firestore to push real-time coordinates to a secure caregiver map.
- ✅ **Task 3.4 — Mission-Critical Hardening**: Move SOS orchestration into a `START_STICKY` Foreground Service.

---

## 🚀 Current System Status (Testing Ready 🚀)
The KAAVAL standalone Android application is fully functional and active for **student field-testing**. Detailed breakdown available in **[docs/STATUS.md](docs/STATUS.md)**.

### Component Status Matrix
| Component | Status | Description |
| :--- | :---: | :--- |
| **SOS Engine** | ✅ **COMPLETE** | Persistent session-based logic with background recovery and single-active incident protection. |
| **Location Engine**| ✅ **COMPLETE** | Continuous multi-stage GPS tracking (30s interval) owned by Foreground Service. |
| **User Feedback** | ✅ **COMPLETE** | Synchronized multi-language TTS, "Happy Haptics", and Stealth mode with gesture confirmation. |
| **Tracking Cloud** | ✅ **COMPLETE** | Real-time Firestore sync with Leaflet.js Caregiver Portal and 4-hour auto-expiration. |
| **Hardware Bridge** | 🟡 **PENDING** | `KaavalBleManager` GATT client built; awaiting physical wearable module for pairing. |
| **Response Loop**  | 🟡 **IN PROGRESS**| Implementing Caregiver Escalation and loops-closure (Sprint 4). |

### Key Features
- 📍 **Continuous Live Tracking:** Background GPS updates pushed to cloud every 30s.
- 🛡️ **Stealth Mode & Blackout UI:** Hidden SOS activity with "Secret Handshake" (Gesture) safety confirmation.
- 🔄 **Crash-Resilient SOS:** Persistent Room-backed sessions that survive process death and activity recreation.
- 🔊 **Synchronized Audio/Haptics:** Audio countdown synced with tactile pulses.
- 🎙️ **Multi-Trigger Redundancy:** English/Malayalam voice + Volume Up x3 hardware key trigger.
- 📩 **Offline SMS Detection:** Automatic reply detection ("OK"/"Coming") from caregivers.

---

## 📁 Repository Structure & Documentation
- `android/` — Kotlin / Jetpack Compose Android Application (MVVM, Clean Architecture, Hilt, Room, Fused Location).
- `functions/` — Firebase Cloud Functions for incident coordination & notifications.
- `tracking-web/` — Web portal for live location tracking & caregiver dashboard.
- `firebase/` — Firebase security rules, cloud architecture, & emulator config.
- `docs/` — [Official Project & Engineering Documentation Hub](docs/README.md).

---

## 👥 Team & Acknowledgments
* **Software Lead & Builder**: Kevin George
* **Hardware & Electrical Engineering Team**: Navami, Adwaid, Jewel *(Electrical & Electronics Engineering)*
* **Program Mentorship**: Supported by the **IEEE Sensors Council Industry Mentoring Program** with OpenAI API & Codex support.
