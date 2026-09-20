<div align="center">
  <img src="../kaaval-logo.png" alt="KAAVAL Official Logo" width="110" />
  
  # KAAVAL Documentation Hub
  
  <p><strong>Accessibility-First Emergency Response Ecosystem for Visually Impaired Individuals</strong></p>
</div>

Supported by the **IEEE Sensors Council Industry Mentoring Program**.

---

## 🗂️ Documentation Directory Index

### 🎯 1. Product Baseline & System Status (`/docs`)
- 📄 **[STATUS.md](STATUS.md)** — Latest release status, complete feature matrix, and student field-testing roadmap.
- 📄 **[SCOPE.md](product/SCOPE.md)** — Comprehensive product scope baseline, user stories, and acceptance criteria.

---

### 🏛️ 2. System Architecture & Hardware (`/docs/architecture`)
- 📄 **[SYSTEM_ARCH.md](architecture/SYSTEM_ARCH.md)** — High-level system architecture, data flow, and emergency state machine.
- 📄 **[TECH_STACK.md](architecture/TECH_STACK.md)** — Detailed technology stack decisions (Android, Jetpack Compose, Room, DataStore, Firebase, OpenAI API).
- 📄 **[WEARABLE_SPEC.md](architecture/WEARABLE_SPEC.md)** — BLE Wearable hardware specification for EEE team (ESP32-C3 / nRF52840, LiPo charging, tactile button).
- 📄 **[THEME_ACCESSIBILITY.md](architecture/THEME_ACCESSIBILITY.md)** — High-contrast Material 3 theme architecture, WCAG AAA compliance (~19.5:1 ratio), and AMOLED battery savings.

---

### 🛠️ 3. Engineering & Accessibility Specifications (`/docs/engineering`)
- 📄 **[VOICE_FEEDBACK.md](engineering/VOICE_FEEDBACK.md)** — `VoiceFeedbackManager` Singleton engine.
- 📄 **[HAPTIC_FEEDBACK.md](engineering/HAPTIC_FEEDBACK.md)** — `HapticFeedbackManager` Singleton engine.
- 📄 **[IMPLEMENTATION.md](engineering/IMPLEMENTATION.md)** — Software architecture design & Clean Architecture module breakdown.

---

### 🛡️ 4. Mission-Critical Reliability & QA (`/docs/QA`)
- 📄 **[KAAVAL_ADVERSARIAL_TEST_MATRIX.md](QA/KAAVAL_ADVERSARIAL_TEST_MATRIX.md)** — 30-Scenario Stress Test (Kidnapping, Snatched Phone, Unconscious User).
- 📄 **[POST_AUDIT_ARCHITECTURE.md](QA/POST_AUDIT_ARCHITECTURE.md)** — Transition from Event-Driven SOS to Persistent Emergency Sessions.
- 📄 **[SPRINT3B_VALIDATION_REPORT.md](QA/SPRINT3B_VALIDATION_REPORT.md)** — Physical device testing for background GPS and activity destruction.

---

### 📈 5. Development Milestones (Sprint Docs)
- 📄 **[SPRINT3_EMERGENCY_SESSION.md](SPRINT3_EMERGENCY_SESSION.md)** — Persistent session logic & Room storage.
- 📄 **[SPRINT3_LOCATION_ENGINE.md](SPRINT3_LOCATION_ENGINE.md)** — 3-Layer GPS fallback & Fused Location.
- 📄 **[SPRINT3_LIVE_TRACKING.md](SPRINT3_LIVE_TRACKING.md)** — Secure Firestore sync & Caregiver Web Portal.

---

### 🚀 6. Operations, Deployment & Presentation Runbooks (`/docs`)
- 📄 **[PRODUCT_DEMO_RUNBOOK.md](PRODUCT_DEMO_RUNBOOK.md)** — Comprehensive online presentation playbook, judge deliverables, screen arrangements, and minute-by-minute pitch script.
- 📄 **[LAUNCH_AND_VERIFICATION.md](runbooks/LAUNCH_AND_VERIFICATION.md)** — Step-by-step commands to build, test, flash, and verify all 4 ecosystem pillars.

---

## 👥 Project Team
- **Software & AI Lead**: Kevin George
- **Hardware & Electrical Team**: Navami, Adwaid, Jewel *(EEE)*
- **Project Mentor**: Hemang Mohan
