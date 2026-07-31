# KAAVAL
**Accessibility-First Emergency Response Ecosystem for Visually Impaired Individuals**

Supported by the **IEEE Sensors Council Industry Mentoring Program**.

---

## 👁️ Vision
> **Our goal is not to build another emergency app.**
> 
> Our goal is to build an **accessibility-first emergency response ecosystem** that enables visually impaired individuals to request help instantly and ensures that caregivers coordinate an effective response until the user is safe.

---

## 🚨 Problem Statement
Visually impaired individuals face three critical challenges during emergencies because traditional apps assume users can quickly locate, unlock, and navigate a smartphone screen:

1. **Emergency Activation**: A visually impaired person may not be able to locate or operate a smartphone during high-stress situations.
2. **Response Assurance**: After triggering an SOS, the user has no feedback or confidence that someone has seen the alert and is responding.
3. **Caregiver Coordination**: Multiple family members receive the alert, but nobody knows who is actively responding, causing confusion and delayed assistance.

---

## 🛡️ Our Solution & Differentiator
KAAVAL manages the **complete emergency response workflow** — from emergency activation to caregiver acknowledgement, live tracking, and incident closure.

* *Android Emergency SOS sends an emergency alert.*
* **KAAVAL coordinates the entire emergency response.**

***This is our biggest differentiator.***

### System Ecosystem:
- ⌚ **BLE Wearable**: Single tactile trigger button with haptic feedback.
- 📱 **User Mobile Application**: Voice-first, high-contrast, TalkBack accessible interface.
- 👨‍👩‍👧 **Caregiver Web Portal**: Emergency acknowledgement, live ETA tracking, & map pin coordination.
- ☁️ **Cloud Coordination Platform**: Incident state engine, tokenized links, and push routing.

---

## 🌐 Live Web Ecosystem Deployment
- 📱 **Live Interactive Web Simulator**: [https://kaaval-94c1d.web.app](https://kaaval-94c1d.web.app)
- 📡 **Caregiver Live Location Portal**: [https://kaaval-94c1d.web.app/live](https://kaaval-94c1d.web.app/live)

---

## 📈 Completed Sprint Milestones

### ✅ Sprint 1 — Project Foundation (`M1`)
- Repository structure, Android Compose shell, Room DB local persistence, Firebase setup, and local web simulator.

### ✅ Sprint 2 — Accessibility Infrastructure & Engines (`M2`)
- ✅ **Task 2.1 — High-Contrast Accessibility Theme**: Material 3 Pure Black (`#000000`) & KAAVAL Yellow (`#FFD600`) design system, typography, and shapes ([THEME_ACCESSIBILITY.md](docs/architecture/THEME_ACCESSIBILITY.md)).
- ✅ **Task 2.2 — TalkBack Accessibility**: Full Jetpack Compose Semantics, explicit `contentDescription`, `Role.Button`/`Role.Tab`, and dynamic `stateDescription` announcements ([TALKBACK_ACCESSIBILITY.md](docs/engineering/TALKBACK_ACCESSIBILITY.md)).
- ✅ **Task 2.3 — Voice Feedback Engine**: `VoiceFeedbackManager` Singleton engine, TextToSpeech integration, queued & priority announcements, English baseline & Malayalam-ready architecture ([VOICE_FEEDBACK.md](docs/engineering/VOICE_FEEDBACK.md)).
- ✅ **Task 2.4 — Haptic Feedback Engine**: `HapticFeedbackManager` Singleton engine, `VibratorManager` (API 31+) & `Vibrator` fallback (API 26+), 14 `HapticPattern` waveforms ([HAPTIC_FEEDBACK.md](docs/engineering/HAPTIC_FEEDBACK.md)).

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
