# 🗺️ KAAVAL Engineering & Mentoring Progress Roadmap

---
**Document Version:** 1.0  
**Purpose:** Shareable weekly execution plan for IEEE Mentoring & Project Guide reviews  
**Project:** KAAVAL — Accessibility-First Emergency Response Platform  
**Target Platform:** Android MVP (English & Malayalam)  
---

## MVP Outcome

By the end of this plan, a visually impaired user will be able to hold a large accessible SOS control, cancel an accidental trigger, alert all saved emergency contacts by SMS, initiate a call to the Primary Emergency Contact, share live location through a secure time-limited link when available, mark themselves safe, and review emergency history.

The MVP does not include wearable hardware, caregiver applications, AI, medical profiles, QR profiles, iOS, push notifications, or caregiver coordination.

## How Weekly Reviews Work

Each week, the student presents:

1. A working demonstration on a real Android device where applicable.
2. The completed deliverables and test evidence.
3. Risks, limitations, and decisions that need guide input.
4. The plan for the following week.

No sprint is considered complete merely because code exists. It must meet the stated success criteria and be testable.

## Weekly Plan

| Week | Focus | Demonstrable outcome | Guide review focus |
|---:|---|---|---|
| 1 | Project setup and feasibility | Buildable app shell, repository baseline, key platform risks documented | Scope, Android/Play feasibility, technical direction |
| 2 | Authentication | Accessible sign-in and persistent user profile | Ease of onboarding and privacy |
| 3 | Emergency contacts | Contact management and Primary Contact selection | Setup simplicity and data handling |
| 4 | SOS interaction | Three-second hold, five-second cancel countdown, accessible feedback | Accessibility and false-trigger prevention |
| 5 | SMS and calling | SMS initiation to contacts and primary-call initiation on real devices | Emergency reliability and platform constraints |
| 6 | Tracking backend | Secure emergency-event and tracking-session foundation | Security, privacy, link expiry |
| 7 | Live location | Recipient can view active location through secure link | Tracking usefulness and location-failure behavior |
| 8 | Emergency closure | Mark-safe flow and automatic 60-minute expiry | User control and lifecycle correctness |
| 9 | Event history and recovery | Accessible history; recovery after interruption/network loss | Reliability and truthful status handling |
| 10 | Accessibility hardening | TalkBack, Malayalam, large text, contrast, and touch-target audit | Real accessibility quality |
| 11 | End-to-end validation | Full flow under normal and failure conditions | Readiness, risks, and limitations |
| 12 | Demo and release candidate | Signed internal build, guide demo, documentation | MVP acceptance and next-phase direction |

---

## Week 1 — Project Setup and Feasibility

**Objective**

Create a safe, reproducible development foundation before feature development begins.

**Planned work**

- Initialize the Git repository and approved folder structure.
- Create Kotlin/Jetpack Compose project foundation and continuous-integration checks.
- Configure development environment separation and secret-handling rules.
- Establish accessible UI, English/Malayalam resource, and testing baselines.
- Confirm test-device availability.
- Validate or document open decisions for SMS permission approval, automatic calling, offline tracking-link behavior, and location-data retention.

**Evidence to show the guide**

- Repository structure and planning documents.
- App shell running on a physical Android device.
- CI build/test result.
- Risk/decision log for SMS, calling, offline tracking, and privacy.

**Success criteria**

- The project builds consistently.
- No secrets are committed.
- The app’s initial screen is usable with TalkBack.
- The high-risk platform assumptions have an evidence-backed status.

## Week 2 — Authentication and User Profile

**Objective**

Allow users to access a personal KAAVAL account and retain profile information securely.

**Planned work**

- Implement accessible phone-number OTP authentication.
- Create user profile creation and update flow.
- Apply backend authorization so users access only their own profile.

**Evidence to show the guide**

- Sign-in and retry flow on a real device.
- TalkBack walkthrough of the OTP flow.
- Authorization/security-rule test results.

**Success criteria**

- User can sign in and return to the same profile.
- Failed authentication provides clear accessible feedback.
- One user cannot access another user’s data.

## Week 3 — Emergency Contact Setup

**Objective**

Let users configure trusted contacts and identify one Primary Emergency Contact.

**Planned work**

- Add, edit, validate, and delete emergency contacts.
- Require the user to select exactly one Primary Emergency Contact.
- Prevent SOS use until a Primary Contact is configured, with an accessible explanation.

**Evidence to show the guide**

- Contact-management demo.
- Primary-contact selection demonstration.
- Test evidence for validation and data ownership.

**Success criteria**

- Contacts are easy to manage with TalkBack.
- Exactly one Primary Contact is enforced.
- Contact numbers are handled as sensitive data and never logged.

## Week 4 — Accessible SOS Activation

**Objective**

Build the safety-critical activation experience without triggering real alerts yet.

**Planned work**

- Implement large SOS control.
- Require a three-second hold.
- Present a five-second cancellation countdown.
- Add voice guidance, haptic feedback, TalkBack status, large-text, and high-contrast support.
- Persist the active emergency state locally.

**Evidence to show the guide**

- Demo of early release, countdown cancellation, and successful activation.
- TalkBack-led SOS demo.
- State-machine test results.

**Success criteria**

- Accidental presses do not activate emergencies.
- The user can cancel during the approved time window.
- The experience does not rely on visual feedback alone.

## Week 5 — Emergency SMS and Primary Calling

**Objective**

Connect the SOS event to real emergency notification behavior.

**Planned work**

- Request Android permissions accessibly.
- Initiate SMS delivery to all emergency contacts.
- Initiate the approved call behavior to the Primary Emergency Contact after SMS initiation.
- Report actual initiation outcomes without claiming SMS delivery or call connection.
- Confirm behavior when GPS is unavailable.

**Evidence to show the guide**

- Real-device, multi-contact SMS/call demonstration.
- Permission-denied and no-location scenario.
- Google Play policy/compliance status.

**Success criteria**

- SMS/call initiation does not wait for GPS.
- Only the Primary Emergency Contact is called.
- Failure states are understandable and truthful.

## Week 6 — Secure Tracking Foundation

**Objective**

Build the backend safety controls needed before exposing live location.

**Planned work**

- Create emergency-event and tracking-session records.
- Create secure, opaque, time-limited tracking tokens.
- Build server-side session validation, revocation, and expiry rules.
- Build the initial recipient tracking-page shell.

**Evidence to show the guide**

- Tracking-link/token lifecycle demo.
- Security-rule and unauthorized-access test results.
- Data-flow diagram showing location access boundaries.

**Success criteria**

- Tracking links cannot be guessed.
- Expired/revoked links cannot access location.
- Recipients do not need KAAVAL accounts.

## Week 7 — Live Location Sharing

**Objective**

Allow contacts to locate the user while an emergency is active.

**Planned work**

- Obtain best available device location through Android services.
- Run active tracking using a compliant visible foreground process.
- Upload location updates to the active tracking session.
- Show latest location as accessible text, a map, and an external-navigation link.
- Apply the approved offline behavior and continue without blocking alerts.

**Evidence to show the guide**

- SOS-to-tracking-link demonstration across two devices.
- Indoor/outdoor and temporary-network-loss test results.
- Recipient tracking page accessibility check.

**Success criteria**

- Contacts can view only the current authorized emergency location.
- Location failure never blocks SMS/calling.
- Tracking starts when usable location becomes available.

## Week 8 — Emergency Closure and Expiry

**Objective**

Give the user control over ending an emergency and enforce the approved tracking limit.

**Planned work**

- Implement mark-safe flow.
- Stop tracking and revoke the link at mark-safe.
- Implement automatic 60-minute tracking expiry.
- Add accessible closure status feedback.

**Evidence to show the guide**

- Mark-safe demonstration from the user and recipient sides.
- Controlled test of automatic expiry.

**Success criteria**

- Marking safe immediately ends tracking access.
- Tracking cannot continue beyond 60 minutes.
- The user receives clear confirmation.

## Week 9 — Emergency History and Resilience

**Objective**

Provide an accessible record of events and handle interruptions safely.

**Planned work**

- Implement emergency history and event details.
- Recover pending/active emergency state after app restart or process interruption.
- Handle network restoration and avoid duplicate events/updates.
- Apply approved location/event retention behavior.

**Evidence to show the guide**

- History demonstration.
- Process interruption/restart test.
- Network-loss/recovery test.

**Success criteria**

- History states are accurate and accessible.
- A restart does not silently lose an active emergency.
- Retries do not create duplicate emergency events.

## Week 10 — Accessibility and Quality Hardening

**Objective**

Validate that KAAVAL is truly accessibility-first rather than merely functional.

**Planned work**

- Audit TalkBack labels, focus order, control roles, announcements, and touch targets.
- Verify dynamic text, contrast, English, Malayalam, and screen-reader tracking page support.
- Review logs, analytics, crash reports, and URLs for sensitive-data exposure.
- Fix discovered critical/high defects.

**Evidence to show the guide**

- Accessibility-audit checklist and walkthrough.
- Localization review.
- Privacy/telemetry audit summary.

**Success criteria**

- Critical workflow is usable non-visually.
- No status depends on color alone.
- No sensitive location/contact/token data appears in telemetry.

## Week 11 — End-to-End Validation

**Objective**

Test the full MVP under realistic normal and failure conditions.

**Planned work**

- Execute full end-to-end test scripts.
- Test GPS unavailable, data unavailable, denied permissions, carrier/SMS problems, call failures, backgrounding, and expired links.
- Test on the agreed Android-device matrix.
- Record known limitations honestly.

**Evidence to show the guide**

- End-to-end test report.
- Defect list with severity and resolution status.
- Known-limitations register.

**Success criteria**

- No critical/high defect remains open.
- Approved MVP acceptance criteria are either passed or have an explicitly approved limitation.

## Week 12 — Demo and Release Candidate

**Objective**

Prepare a stable, repeatable demonstration build for the mentorship program.

**Planned work**

- Build and sign internal release candidate.
- Distribute to testers using the chosen internal-distribution route.
- Prepare demo script, tester guide, release notes, and feedback form.
- Present complete normal and degraded-location emergency scenarios.
- Capture Phase 2 backlog from guide/tester feedback without expanding the MVP.

**Evidence to show the guide**

- Signed internal build.
- Repeatable real-device demo.
- Final test report and documentation pack.

**Success criteria**

- The complete approved MVP works in a release build.
- The demonstration is repeatable and transparent about limitations.
- Post-MVP requests are recorded separately from the frozen MVP scope.

## Weekly Progress Report Template

Use this short format before each guide meeting:

```text
Week:
Sprint / feature:

Completed:
-

Demonstration available:
-

Tests completed:
-

Accessibility checks completed:
-

Risks / blockers:
-

Decision needed from guide:
-

Next week:
-
```

## Guide Decision Checkpoints

The project guide should be specifically asked to review these points:

| Checkpoint | Week | Decision / feedback needed |
|---|---:|---|
| Platform feasibility | 1 | SMS permissions, calling path, offline tracking-link behavior, data retention |
| User onboarding | 2–3 | Authentication and contact setup simplicity |
| Activation safety | 4 | Hold/countdown/cancellation suitability for visually impaired users |
| Alerting reliability | 5 | Truthful status language and real-device behavior |
| Location privacy | 6–8 | Token/link protection, expiry, and user control |
| Accessibility quality | 10 | TalkBack and Malayalam readiness |
| MVP acceptance | 11–12 | Whether the demonstration proves the defined MVP outcome |

