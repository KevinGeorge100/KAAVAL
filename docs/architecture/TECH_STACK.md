# 🛠️ KAAVAL Technology Stack Specification

---
**Document Version:** 1.0  
**Status:** Approved Technology Stack Baseline  
**Target Platform:** Native Android (Kotlin & Jetpack Compose)  
**Cloud Infrastructure:** Firebase (Firestore, Auth, Hosting, Functions, Cloud Messaging)  
**AI Engine:** OpenAI API (Emergency Incident Summarization)  
---

## Executive Recommendation

Build the Android MVP natively with **Kotlin and Jetpack Compose**, backed by **Firebase** services, with Android platform APIs handling SMS, calling, location, and foreground tracking.

This is not the smallest possible amount of code. It is the smallest amount of *risk* for KAAVAL’s critical path. The MVP needs Android-specific behavior—TalkBack semantics, runtime permissions, a live-location foreground service, direct SMS initiation, and call initiation. A native Android implementation gives the clearest access to, and control over, those platform capabilities. Jetpack Compose has first-party accessibility semantics and testing support. [Compose accessibility documentation](https://developer.android.com/develop/ui/compose/accessibility)

Firebase is recommended because it provides a fast, managed path to authentication, event data, secure server-side functions, tracking-page hosting, crash reporting, analytics, and test distribution. It is appropriate for the MVP and can scale past it, while the application architecture keeps vendor APIs behind repository/service interfaces.

## Evaluation Scale

- **Learning curve:** Low is easier for a new developer.
- **Scalability, speed, community, accessibility, maintainability:** High is better.
- **Cost:** Low is better for the MVP.

Ratings are relative to KAAVAL’s requirements, not universal rankings.

## 1. Mobile Framework

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Kotlin + Jetpack Compose (native Android) | Medium | High | Medium | Low | High | **High** | **High** |
| Flutter | Medium | High | High | Low | High | Medium–High | High |
| React Native | Medium–High | Medium–High | High | Low | High | Medium | Medium |

**Recommendation: Kotlin + Jetpack Compose.**

Flutter remains a reasonable future cross-platform option, and it was proposed in early project material. However, the approved MVP is Android-only and depends on sensitive Android integrations. Kotlin removes the bridge/plugin layer from the most critical behavior and gives direct, documented use of Android services. Compose supports semantic roles, traversal order, scalable content, and accessibility checks; these are central to KAAVAL, not cosmetic additions. [Android accessibility guidance](https://developer.android.com/develop/ui/compose/accessibility)

**Trade-off:** Flutter would accelerate a later iOS client. The recommended architecture mitigates this by keeping business rules independent of UI and Android adapters, so an iOS client can be added later without rewriting backend contracts.

## 2. Programming Language

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Kotlin | Medium | High | High | Low | High | High | **High** |
| Java | Medium | High | Medium | Low | High | High | Medium |
| Dart (with Flutter) | Low–Medium | High | High | Low | High | Medium–High | High |

**Recommendation: Kotlin for Android; TypeScript for Firebase server functions.**

Kotlin is Android’s modern primary language, works naturally with Compose, coroutines, typed models, and Android’s architecture libraries. TypeScript is recommended for the small backend-function surface because its static typing reduces mistakes in security-sensitive event and tracking-link code while remaining approachable for a solo developer.

## 3. State Management

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| ViewModel + Kotlin Flow/StateFlow + unidirectional state | Medium | High | High | Low | High | Neutral | **High** |
| Redux/MVI library | Medium–High | High | Medium | Low | Medium | Neutral | High |
| Mutable UI state spread through screens | Low initially | Low | Medium initially | Low | High | Neutral | Low |

**Recommendation: Android ViewModel + StateFlow, with unidirectional data flow.**

Each feature exposes an immutable UI state and accepts explicit user events. The emergency feature owns a small explicit state machine (`Ready → Hold → Countdown → Activating → Active → Closed`). This makes cancellation, lifecycle recovery, and accessible status feedback testable. Avoid a third-party state library initially; it would add terminology without reducing the MVP’s core complexity.

## 4. Backend Platform

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Firebase (Auth, Firestore, Functions, Hosting) | **Low** | High | **High** | Low initially / usage-based | High | Neutral | High |
| Supabase (Auth, Postgres, Edge Functions) | Medium | High | High | Low initially / usage-based | High | Neutral | High |
| Custom Node.js API + managed database | High | High | Medium–Low | Medium | High | Neutral | Medium–High |

**Recommendation: Firebase.**

Firebase provides the fastest managed path for a solo MVP: authenticated mobile SDKs, server functions for sensitive logic, Firestore for event/session data, hosting for the recipient tracking page, Crashlytics, Analytics, App Check, and App Distribution. Cloud Functions scale instances automatically and can expose HTTP endpoints or react to backend events. [Cloud Functions documentation](https://firebase.google.com/docs/functions)

Use Firebase in a **thin-backend** style: client-side reads/writes are limited by security rules; tracking token creation, token validation, session closure/expiry, and any privileged operation run in Cloud Functions. Do not put secret keys or authorization decisions in the Android client.

**Trade-off:** Firebase creates vendor coupling and requires a Blaze pay-as-you-go plan to deploy Cloud Functions. The dependency is acceptable at MVP scale, but repositories and backend service interfaces must keep a later migration possible. [Firebase pricing documentation](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans)

## 5. Database

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Cloud Firestore | Low | High | High | Usage-based | High | Neutral | High |
| PostgreSQL via Supabase | Medium | High | Medium | Usage-based | High | Neutral | High |
| Custom PostgreSQL/MySQL | High | High | Medium–Low | Medium | High | Neutral | Medium–High |

**Recommendation: Cloud Firestore.**

The MVP’s primary data is document-shaped: user profile, contact list, emergency event, tracking session, and location updates. Firestore avoids schema-migration and server-management overhead while supporting real-time reads for the live tracking page. It is managed and scalable, but billing is based on reads, writes, deletes, storage, and network usage. [Firestore billing model](https://firebase.google.com/docs/firestore/pricing)

Design requirements:

- Keep location updates in a collection separate from the event summary.
- The tracking page should read only the latest location, not stream the entire location history.
- Define retention before release, then delete or aggregate old location points.
- Use security rules plus Cloud Functions; rules are not a replacement for server-side validation of tracking tokens.

## 6. Authentication

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Firebase Phone Authentication (OTP) | Low–Medium | High | High | Per-SMS cost | High | High | High |
| Email/password | Low | High | High | Low | High | Medium | Medium |
| Google Sign-In only | Low | High | High | Low | High | Medium | Medium |

**Recommendation: Firebase Phone Authentication with OTP, plus email/password only if later needed.**

A phone number fits a mobile-first safety product in India, reduces password-management burden, and helps with account recovery. The OTP interaction must be designed and tested with TalkBack, with clear spoken instructions and a manual-entry fallback. Phone authentication is usage-billed, so rate limiting, abuse controls, and budget alerts are mandatory. [Firebase pricing](https://firebase.google.com/pricing)

Emergency contacts do not authenticate and must never be modeled as KAAVAL users in the MVP.

## 7. Maps

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Google Maps Platform (Maps SDK + Maps JavaScript API) | Low | High | High | Usage-based | **High** | High with accessible alternatives | High |
| Mapbox | Medium | High | High | Usage-based | High | Medium–High | High |
| OpenStreetMap + self-hosted/third-party tiles | Medium–High | Medium–High | Medium | Low–Medium | High | Medium | Medium |

**Recommendation: Google Maps Platform.**

Use a map only where it helps: the recipient tracking page. Provide the same location in accessible text and a link to open the user’s preferred map/navigation app; the visual map cannot be the only source of information. Google Maps has mature Android/web SDKs and broad India coverage, reducing integration risk. Billing and quotas must be configured from day one. [Google Maps SDK usage and billing](https://developers.google.com/maps/documentation/android-sdk/usage-and-billing)

## 8. Location Services

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Google Play services Fused Location Provider | Low–Medium | High | High | Low | High | Neutral | High |
| Android `LocationManager` directly | Medium | High | Medium | Low | High | Neutral | Medium |
| Third-party abstraction library | Low initially | Medium | Medium | Low | Medium | Neutral | Medium |

**Recommendation: Fused Location Provider, wrapped behind a `LocationProvider` interface.**

It combines device location sources, supports last-known and ongoing updates, and lets the app express quality/power needs rather than managing GPS and Wi-Fi providers manually. Android recommends the Google Play services location APIs for current location and periodic updates. [Android location guidance](https://developer.android.com/develop/sensors-and-location/location/migration)

Live tracking must run through a visible, compliant Android location foreground service after the user activates SOS; it must never block the initial SMS/call path. Android imposes strict background-start and location-permission rules, so this must be implemented and tested natively. [Foreground service guidance](https://developer.android.com/develop/background-work/services)

## 9. SMS and Calling Integration

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Native Android SMS + call APIs | Medium | High | High | Carrier-funded | High | High | High |
| Backend SMS provider + Android call intent | Medium | High | Medium | Per-message cost | High | Medium | High |
| SMS deep link / dial intent only | Low | High | Medium | Carrier-funded | High | High | High |

**Recommendation: Native Android APIs as the primary path, behind `SmsGateway` and `CallInitiator` interfaces.**

The approved MVP requires alerting not to wait for internet, which rules out a backend-only SMS path. Device-side SMS can use the user’s carrier connection, and device-side call initiation is the most direct path to the designated Primary Emergency Contact. Isolating both behind interfaces preserves a future fallback provider or policy-driven implementation.

**Mandatory release gate:** Validate the exact permission, user-consent, Android-version, and Google Play policy path before implementation begins. Google Play identifies physical-safety emergency SMS as a permitted `SEND_SMS` use case, subject to declaration/review. Its policy also notes that a dial intent does not need call permission but requires the user to explicitly initiate the call, which may conflict with KAAVAL’s automatic-call requirement. [Google Play SMS/Call Log policy](https://support.google.com/googleplay/android-developer/answer/10208820?hl=en)

Do not promise SMS delivery, read receipts, or completed calls; the MVP can record initiation attempts only.

## 10. Secure Local Storage

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Android Keystore + DataStore + Room | Medium | High | High | Low | High | Neutral | High |
| SharedPreferences only | Low | Low | High initially | Low | High | Neutral | Low |
| SQLCipher for all local data | Medium–High | High | Medium | Low | High | Neutral | Medium–High |

**Recommendation: Android Keystore for cryptographic keys/secrets, DataStore for non-sensitive preferences, and Room for durable emergency state and queued synchronization.**

Store the minimum sensitive data locally. The active emergency record must survive app interruption, but raw location history should not be retained longer than necessary. Android recommends the Keystore when stronger key security is required. [Android Keystore guidance](https://developer.android.com/privacy-and-security/keystore)

This is a pragmatic MVP choice: it protects secrets appropriately without introducing application-managed database encryption complexity. Reassess SQLCipher only if a threat model or compliance need requires encryption of the local Room database itself.

## 11. Dependency Injection

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Hilt | Medium | High | High | Low | High | Neutral | **High** |
| Koin | Low | Medium–High | High | Low | High | Neutral | High |
| Manual dependency wiring | Low initially | Low | Medium | Low | High | Neutral | Low |

**Recommendation: Hilt.**

Hilt is Android’s recommended DI solution, integrates cleanly with Compose and ViewModels, and makes it straightforward to swap real SMS/location/backend adapters for fakes in tests. It has more initial concepts than Koin, but its compile-time checking is valuable for a safety-critical workflow. [Android Hilt guidance](https://developer.android.com/training/dependency-injection/hilt-android)

## 12. Networking

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Firebase Android SDKs + Retrofit/OkHttp for explicit HTTP endpoints | Medium | High | High | Low | High | Neutral | High |
| Ktor Client only | Medium | High | Medium | Low | High | Neutral | High |
| Raw HTTP client calls | Low initially | Medium | Medium | Low | High | Neutral | Low |

**Recommendation: Firebase Android SDKs for Firebase services, plus Retrofit and OkHttp for any explicit HTTPS API boundary.**

Firebase SDKs reduce code for authentication and Firestore. Retrofit/OkHttp provides typed, interceptable, testable HTTP calls when Cloud Functions are exposed as explicit APIs or when a future provider is added. Centralize retry, timeout, authentication, and redaction policies in one network layer. The emergency state machine—not generic HTTP retries—decides whether an operation is safe to retry.

## 13. Logging

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Small `AppLogger` interface + Timber in debug builds | Low–Medium | High | High | Low | High | Neutral | High |
| Direct Android `Log` calls | Low | Medium | High | Low | High | Neutral | Low |
| Full external logging platform | Medium | High | Medium | Medium | High | Neutral | Medium |

**Recommendation: an application logging interface, implemented with Timber for development and tightly controlled production logging.**

This lets the project enforce a single rule: never log phone numbers, precise location, tracking tokens, SMS body, authentication tokens, or user-entered personal information. Log structured event IDs and outcome categories instead.

## 14. Crash Reporting

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Firebase Crashlytics | Low | High | High | Low initially | High | Neutral | High |
| Sentry | Medium | High | High | Usage-based | High | Neutral | High |
| Bugsnag | Medium | High | High | Usage-based | High | Neutral | High |

**Recommendation: Firebase Crashlytics.**

It integrates with the recommended backend ecosystem and gives a solo developer fast visibility into crashes. Configure custom keys only with non-sensitive diagnostic data, such as app version, Android version, and an opaque event state—not user data or location. Crashlytics is included as a no-cost Firebase product, subject to product limits. [Firebase pricing plans](https://firebase.google.com/docs/projects/billing/firebase-pricing-plans)

## 15. Analytics

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Firebase Analytics with a strict event allowlist | Low | High | High | Low | High | Neutral | High |
| PostHog | Medium | High | Medium | Low–Medium | High | Neutral | High |
| No product analytics | Low | Low | High initially | Low | N/A | Neutral | Medium |

**Recommendation: Firebase Analytics with a minimal, privacy-reviewed event allowlist.**

Track only aggregate product health signals—for example onboarding completion, permission outcome category, SOS flow stage, cancellation, and error category. Never track precise location, contact phone numbers, tracking tokens, SMS/call contents, or a user’s emergency narrative. Make analytics disclosure and consent decisions part of the PRD/privacy work.

## 16. Testing

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| JUnit + MockK + Turbine + Compose UI tests + Hilt tests + Firebase Test Lab | Medium | **High** | Medium | Low–usage-based | High | **High** | High |
| Unit tests only | Low | Low | High initially | Low | High | Low | Low |
| Manual testing only | Low | Low | Medium initially | Low | N/A | Low | Low |

**Recommendation: a layered Android test stack.**

- **Unit tests:** emergency state machine, countdown/cancel behavior, contact validation, expiry, retry/idempotency decisions.
- **Flow tests:** StateFlow emissions and error paths.
- **Instrumented tests:** Compose UI, TalkBack semantics, permission states, and Android adapters where feasible.
- **Integration tests:** Firebase Local Emulator Suite for security rules and Functions.
- **Device tests:** Firebase Test Lab plus physical Android devices for TalkBack, SMS, calls, GPS, poor network, and background tracking.
- **Manual accessibility testing:** mandatory with TalkBack; automated checks do not replace it. Android specifically recommends manual TalkBack testing alongside accessibility scanning. [Android accessibility codelab](https://developer.android.com/codelabs/jetpack-compose-accessibility)

Hilt supports dependency replacement with fakes in automated UI/integration tests. [Hilt testing guide](https://developer.android.com/training/dependency-injection/hilt-testing)

## 17. CI/CD

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| GitHub Actions + Firebase App Distribution + Play internal testing | Medium | High | High | Low initially | High | Neutral | High |
| Bitrise | Low–Medium | High | High | Usage-based | High | Neutral | High |
| Manual builds/releases | Low | Low | Medium initially | Low | High | Neutral | Low |

**Recommendation: GitHub Actions, Firebase App Distribution for tester builds, and Google Play Internal Testing for release-candidate validation.**

Every pull request should compile, run unit tests, run static analysis, and produce a test report. The main branch should also build a signed internal artifact only after secrets are configured safely. Keep production deployment manual until the release process is mature.

## 18. Version Control

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Git + GitHub | Low–Medium | High | High | Low | **High** | Neutral | High |
| GitLab | Medium | High | High | Low–Medium | High | Neutral | High |
| No hosted repository / local Git only | Low | Low | Medium | Low | Low | Neutral | Low |

**Recommendation: Git + GitHub.**

Use a protected `main` branch, short-lived feature branches, pull requests, issue tracking, ADRs, and GitHub Actions. This is the easiest collaboration path when contributors join later.

## 19. Package and Build Management

| Option | Learning curve | Scalability | Development speed | Cost | Community | Accessibility | Maintainability |
|---|---:|---:|---:|---:|---:|---:|---:|
| Gradle Kotlin DSL + Version Catalogs | Medium | High | High | Low | High | Neutral | **High** |
| Gradle Groovy DSL | Medium | High | Medium | Low | High | Neutral | Medium |
| Manually scattered dependency versions | Low initially | Low | Medium | Low | High | Neutral | Low |

**Recommendation: Gradle Kotlin DSL with a Version Catalog.**

It keeps dependency versions centralized, makes updates reviewable, and uses the same language as the Android app. Use Gradle’s dependency locking and automated dependency-update pull requests only after the baseline is stable.

## Final Recommended Stack

| Area | Recommended choice |
|---|---|
| Mobile framework | Native Android, Jetpack Compose |
| Mobile language | Kotlin |
| State management | ViewModel + Kotlin Flow/StateFlow + explicit emergency state machine |
| Architecture | Feature-oriented clean layers; repositories and platform-service interfaces |
| Backend | Firebase: Authentication, Cloud Firestore, Cloud Functions, Hosting, App Check |
| Backend language | TypeScript for Cloud Functions |
| Database | Cloud Firestore |
| Authentication | Firebase Phone Authentication (OTP), pending privacy/cost approval |
| Maps | Google Maps Platform, with accessible text and external-navigation link alternatives |
| Location | Google Play services Fused Location Provider + Android location foreground service |
| SMS / calling | Native Android SMS and calling adapters, isolated behind interfaces; mandatory Play-policy validation |
| Secure local data | Android Keystore + DataStore + Room for durable emergency state |
| Dependency injection | Hilt |
| Networking | Firebase SDKs + Retrofit/OkHttp for explicit HTTPS APIs |
| Logging | `AppLogger` facade + Timber in development; strict PII redaction |
| Crash reporting | Firebase Crashlytics |
| Analytics | Firebase Analytics, minimal privacy-reviewed allowlist |
| Testing | JUnit, MockK, Turbine, Compose UI tests, Hilt tests, Firebase Emulator Suite/Test Lab, physical accessibility testing |
| CI/CD | GitHub Actions + Firebase App Distribution + Google Play Internal Testing |
| Version control | Git + GitHub |
| Build/package management | Gradle Kotlin DSL + Version Catalogs |

## Why This Best Fits KAAVAL and a Solo Developer

The stack is deliberately **native where reliability matters** and **managed where operational burden does not add product value**.

- Kotlin/Compose gives direct access to Android’s emergency-critical capabilities and the strongest first-party accessibility path.
- Firebase avoids operating servers, authentication infrastructure, a database cluster, and a separate crash/analytics/distribution suite while the product is still validating its core workflow.
- Hilt, Flow, Room, and explicit interfaces make the emergency workflow testable and understandable for future contributors.
- The architecture does not lock KAAVAL into a caregiver app, wearable, AI, or iOS implementation before those features enter scope.

## Required Approval Gates Before Implementation

1. Confirm that Firebase and Google Maps Platform are acceptable vendors, including their billing-account requirement.
2. Validate the Google Play `SEND_SMS` declaration path for KAAVAL’s physical-safety emergency alert use case.
3. Prototype and validate a policy-compliant Android mechanism for the required automatic primary-contact call.
4. Decide the approved behavior when internet is unavailable and a secure tracking link cannot be created.
5. Approve phone OTP costs, rate limits, privacy disclosure, and account-recovery rules.
6. Set the minimum Android version, location-update frequency, retention policy, and test-device matrix.

No application implementation should begin until these gates are resolved or consciously accepted as project risks.
