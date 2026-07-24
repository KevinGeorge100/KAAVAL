# KAAVAL
**Accessibility-First Emergency Response Ecosystem for Visually Impaired Individuals**

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
- 👨‍👩‍👧 **Caregiver Application**: Emergency acknowledgement, live ETA tracking, & coordination.
- ☁️ **Cloud Coordination Platform**: Incident state engine, push routing, and escalation logs.

---

## 👥 Users
* **Primary User**: Visually impaired individuals (Every feature directly optimizes their emergency experience).
* **Secondary Users**: Caregivers, Family members, Teachers, NGOs, and Schools for visually impaired individuals.

---

## 📁 Repository Structure
- `android/` — Kotlin / Jetpack Compose Android Application (MVVM, Clean Architecture, Hilt, Room, Fused Location).
- `functions/` — Firebase Cloud Functions for incident coordination & notifications.
- `tracking-web/` — Web portal for live location tracking & caregiver dashboard.
- `firebase/` — Firebase security rules, cloud architecture, & emulator config.
- `docs/` — [Approved Product & Engineering Documentation](docs/README.md).

---

## 🚀 Project Status
Currently building **Phase 1 MVP (Android Application Foundation, Voice/Haptic engine, SOS State Machine, Contacts & Live Tracking)**.

