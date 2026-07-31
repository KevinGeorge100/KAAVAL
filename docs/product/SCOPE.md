# 🎯 KAAVAL MVP Scope Baseline Specification

---
**Document Version:** 1.0  
**Status:** Approved MVP Scope Baseline  
**Target Release:** Android MVP  
**Target Region:** India  
**Primary Languages:** English (`en_US`), Malayalam (`ml_IN`)  
---

## 1. Project Goal

Deliver a reliable, accessibility-first Android mobile application that enables a visually impaired user to request help quickly in an emergency. The MVP must alert the user’s saved emergency contacts, initiate a call to the designated Primary Emergency Contact, and provide live location sharing when location becomes available.

The MVP focuses on initiating help quickly and clearly. It does not attempt to coordinate caregivers through a separate application.

## 2. In Scope

The following features are mandatory for the MVP.

### 2.1 User setup and account access

- User registration and authentication.
- User profile management.
- Emergency-contact management.
- Selection of exactly one Primary Emergency Contact from the user’s emergency contacts.
- Accessibility preferences.

**Authentication method is not yet specified.** It will be decided in the PRD and architecture work without changing this scope baseline.

### 2.2 Accessibility-first experience

- A large, accessible SOS control.
- TalkBack compatibility.
- Voice guidance and voice confirmation of emergency status.
- Haptic (vibration) feedback.
- Large-text and high-contrast accessibility mode.
- English and Malayalam language support.
- Minimal, predictable interaction for the emergency workflow.

### 2.3 Emergency activation and cancellation

- Emergency activation begins when the user holds the SOS control for three seconds.
- A five-second countdown follows the hold.
- The user can cancel during the countdown to prevent an accidental activation.
- If not cancelled, the emergency becomes active.

### 2.4 Emergency notification and calling

- On activation, KAAVAL captures location when it is immediately available.
- KAAVAL initiates SMS delivery to all saved emergency contacts simultaneously.
- The SMS includes the available location or indicates that location is currently unavailable.
- After SMS delivery is initiated, KAAVAL initiates a phone call to the Primary Emergency Contact only.
- The implementation must follow applicable Android platform and Google Play policies for call initiation.

### 2.5 Live location sharing

- Emergency SMS includes a secure live-tracking link.
- Live location sharing begins immediately after activation.
- If an accurate location is not immediately available, alerting and calling are not delayed.
- KAAVAL continues attempting to obtain location in the background and updates the live-tracking session once location becomes available.
- Tracking ends when the user marks themselves safe or after 60 minutes, whichever occurs first.

### 2.6 Emergency lifecycle and history

- The user can mark themselves safe to end an active emergency and stop live tracking.
- Emergency events are stored in an event history / incident log.

### 2.7 Data handled by the MVP

- User profile.
- Emergency contacts, including the Primary Emergency Contact designation.
- Accessibility preferences.
- Emergency-event history.
- Location data associated with an active emergency and live-tracking session.

Location data is sensitive and must be handled securely.

## 3. Out of Scope

The following features are intentionally excluded from the MVP.

- Bluetooth wearable hardware and wearable pairing.
- BLE connectivity, wearable battery monitoring, connection-status monitoring, and wearable feedback controls.
- Dedicated caregiver application or caregiver web dashboard.
- Caregiver accounts, responder acknowledgement, responder status, ETA, navigation, multi-caregiver coordination, contact escalation, and emergency notes.
- Push notifications.
- Artificial intelligence, including emergency summaries, classification, context analysis, and false-trigger analysis.
- Speech recognition and an AI voice assistant.
- Medical profiles, including blood group, allergies, and medication information.
- Emergency QR profiles.
- iOS support.
- Bluetooth wearable hardware development and embedded firmware.
- Cloud synchronization as a separately scoped future capability beyond the services required to operate the MVP’s account, event history, and live-tracking link.

## 4. Future Roadmap

### Phase 2

- Bluetooth-enabled wearable emergency activation.
- Wearable pairing, connection monitoring, battery monitoring, tactile input, vibration feedback, and status indication.
- Dedicated caregiver application.
- Caregiver emergency acknowledgement and response-status updates.
- Multi-caregiver coordination and contact escalation.
- Caregiver live-location tracking, navigation, and estimated arrival time.

### Beyond Phase 2

- AI-assisted emergency context analysis, summaries, situation classification, and false-trigger assistance.
- Speech recognition and AI voice-assistant capabilities.
- Medical profile management and emergency QR profiles.
- Configurable live-location sharing duration.
- iOS support.
- Expanded cloud services and caregiver dashboards.

## 5. User Stories

1. **As a visually impaired user,** I want an accessible, easy-to-find SOS control so that I can request help quickly under stress.
2. **As a user,** I want to hold the SOS control for three seconds and receive a five-second cancellation window so that accidental activations can be avoided.
3. **As a user,** I want all of my emergency contacts alerted simultaneously by SMS so that more than one trusted person can be informed without delay.
4. **As a user,** I want KAAVAL to initiate a call to my designated Primary Emergency Contact after alerting my contacts so that I can attempt direct communication with my most trusted responder.
5. **As a user,** I want my emergency SMS to include a secure live-tracking link so that my contacts can locate me.
6. **As a user,** I want help to be requested even if GPS is temporarily unavailable so that location acquisition never delays an alert or call.
7. **As a user,** I want location updates to begin when location becomes available so that my contacts can locate me after the initial alert.
8. **As a user,** I want to mark myself safe so that live tracking stops when the emergency is over.
9. **As a user,** I want an accessible record of prior emergencies so that I can review my emergency history.
10. **As a user,** I want KAAVAL to support English and Malayalam with TalkBack, voice guidance, haptic feedback, large text, and high contrast so that I can use it independently.

## 6. Success Criteria

The MVP is successful when:

- A visually impaired user can independently initiate an emergency through the defined SOS flow.
- All saved emergency contacts receive an SMS alert without waiting for GPS to become available.
- KAAVAL initiates a call to the user’s Primary Emergency Contact after initiating SMS delivery.
- Contacts can use a secure link to access the active live-location session when location is available.
- The user receives accessible status feedback throughout the activation flow.
- The user can end tracking by marking themselves safe, and tracking ends automatically after 60 minutes if they do not.
- Emergency events appear in the user’s history.

## 7. MVP Acceptance Criteria

| Area | Acceptance criteria |
|---|---|
| SOS activation | Holding the accessible SOS control for three seconds starts a five-second countdown; cancelling in that period prevents activation. |
| Emergency alert | If the countdown completes, SMS delivery is initiated to every saved emergency contact. |
| Primary call | After SMS delivery is initiated, KAAVAL initiates a call to the designated Primary Emergency Contact, subject to Android and Google Play requirements. |
| Immediate location | When an accurate location is immediately available, the alert SMS includes it and the live-tracking link is active. |
| Location unavailable | When location is not immediately available, SMS and call initiation proceed without delay; the SMS indicates location is unavailable, and KAAVAL keeps attempting location updates. |
| Tracking duration | Active tracking stops when the user marks themselves safe or when 60 minutes have elapsed, whichever occurs first. |
| Accessibility | The emergency workflow supports TalkBack, voice guidance, haptic feedback, large text, high contrast, English, and Malayalam. |
| Event history | Activated emergency events are recorded and accessible through emergency history. |
| Contact setup | The user can save emergency contacts and designate one of them as the Primary Emergency Contact. |

## 8. Risks and Limitations

- SMS delivery, call connection, location accuracy, and live tracking depend on device permissions, carrier service, network availability, GPS conditions, and Android platform behavior.
- KAAVAL cannot guarantee that an emergency contact reads an SMS, answers a call, or responds.
- A live-tracking link requires connectivity for updates and for the recipient to view current location.
- Location may be unavailable or inaccurate indoors, in dense urban areas, or where device location services are disabled.
- Android and Google Play policies may limit unattended call placement; the implementation must comply with these rules.
- The MVP does not provide caregiver acknowledgement or coordination, so the user cannot be assured that a contact has taken responsibility for responding.
- The MVP does not integrate with public emergency services.
- The MVP is only for Android and does not include wearable activation.

## 9. Assumptions

- KAAVAL is the final product name; references to “SAHAAYA” in earlier material are legacy naming.
- The MVP is intended for users in India and supports English and Malayalam.
- Users provide valid phone numbers and consent to contacting their emergency contacts and sharing live location during an active emergency.
- A user can maintain one or more emergency contacts and must designate one as Primary before using the primary-call workflow.
- SMS alerting is intended for ordinary phone-number contacts; recipients do not need a KAAVAL account or application.
- The emergency workflow is initiated from the KAAVAL mobile application; Android hardware-button activation is not part of the defined MVP scope.
- “Location currently unavailable” is communicated in the initial emergency SMS when no accurate location is immediately available.
- Privacy, retention, account deletion, authentication method, exact notification wording, tracking-link access controls, and the definition of “accurate location” are not yet specified. They require decisions in the PRD and later design work.

## 10. Non-Goals

The MVP is not intended to:

- Replace public emergency services or guarantee emergency response.
- Guarantee that an emergency contact sees, acknowledges, or acts on an alert.
- Provide coordinated responder management or caregiver accountability.
- Diagnose emergencies, assess medical conditions, or make emergency decisions using AI.
- Function as a medical-record system.
- Support wearable hardware, iOS, or a general-purpose social/caregiver platform.
- Eliminate dependence on a functioning Android device, granted permissions, mobile carrier service, and network/location availability.

---

## Scope Change Control

This document is the official MVP scope baseline. Any addition, removal, or material change must be explicitly reviewed and versioned before implementation.
