# KAAVAL (കാവൽ)

![KAAVAL Emergency Response Ecosystem](banner.png)

### Accessibility-First Emergency Response Ecosystem for Visually Impaired Individuals

[![Android 14 Ready](https://img.shields.io/badge/Android-14%20(API%2029--34)-3DDC84?style=for-the-badge&logo=android&logoColor=white)](android/)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](android/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)](android/)
[![Firebase](https://img.shields.io/badge/Cloud-Firebase%20Firestore%20%26%20Hosting-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://kaaval-94c1d.web.app)
[![ESP32 FreeRTOS](https://img.shields.io/badge/Firmware-ESP32%20BLE%204.2-E7352C?style=for-the-badge&logo=espressif&logoColor=white)](firmware/)
[![Vector Maps](https://img.shields.io/badge/Maps-MapLibre%20GL%20Vector-333333?style=for-the-badge&logo=maplibre&logoColor=white)](https://kaaval-94c1d.web.app/live)
[![Release](https://img.shields.io/badge/Release-v1.0.0--prod-10B981?style=for-the-badge&logo=github&logoColor=white)](https://github.com/KevinGeorge100/KAAVAL/releases/tag/v1.0.0-prod)

Supported by the **IEEE Sensors Council Industry Mentoring Program**.

---

### Quick Access & Distribution

| Destination | Endpoint | Scope |
| :--- | :--- | :--- |
| **Android Client (APK)** | [kaaval-94c1d.web.app/download](https://kaaval-94c1d.web.app/download) | Production Release Build (2.95 MB, R8 Optimized) + Quick-Scan QR |
| **Incident Command Portal** | [kaaval-94c1d.web.app/live](https://kaaval-94c1d.web.app/live) | Real-time WebGL Vector Operations Dashboard |
| **GitHub Release Registry** | [Releases v1.0.0-prod](https://github.com/KevinGeorge100/KAAVAL/releases/tag/v1.0.0-prod) | Cryptographic binaries, release notes, and SHA-256 artifacts |
| **Engineering Hub** | [docs/README.md](docs/README.md) | Technical architecture, hardware schematics, and runbooks |

---

## Executive Overview: Closing the Sensory Gap in Emergency Care

> **Traditional emergency panic buttons are broken by design.**  
> They operate as open-loop, fire-and-forget transmitters: an SMS is dispatched, but the victim stands in total sensory darkness with zero confirmation that anyone heard them or is coming.  
>
> **KAAVAL establishes a closed-loop emergency ecosystem** engineered specifically for visually impaired individuals. It bridges instantaneous mechanical activation with real-time situational intelligence and bidirectional tactile reassurance until the individual is safe.

---

## The Sensory Challenge in Crisis Situations

Every standard personal safety system relies on an untenable assumption: **that the victim has visual acuity, situational composure, and digital dexterity during an emergency.**

```
   Conventional Systems                                KAAVAL Platform
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│ • Screen unlock & app navigation     │     │ • Sub-120ms mechanical trigger      │
│ • Unidirectional "fire-and-forget"   │     │ • Hardware volume key interrupts     │
│ • No feedback; user remains anxious  │     │ • Closed-loop tactile reassurance    │
│ • Uncoordinated broadcast spam       │     │ • Single-claim incident triage map   │
└──────────────────────────────────────┘     └──────────────────────────────────────┘
```

1. **Physical Barrier to Activation**: Unlocking a touchscreen, finding an emergency dialer, or navigating multi-tier digital interfaces during physical distress is impossible without sight.
2. **The Feedback Vacuum**: Once a message is sent, the user receives no non-visual feedback. They cannot know whether their message bounced, was missed, or if responders are en route.
3. **Bystander Inaction & Triage Delay**: Broadcasting SMS alerts to multiple family members simultaneously without state management leads to redundant calls, conflicting actions, or diffusion of responsibility.

---

## Technical Architecture: 4-Pillar Synchronization

KAAVAL operates as an event-driven distributed system divided into four distinct pillars:

```mermaid
flowchart TD
    subgraph P1 ["Pillar 1: Embedded Hardware"]
        A["KAAVAL Wearable Wristband"] -->|BLE 4.2 GATT Notification| B["Mechanical SPST Trigger"]
        K["LRA/ERM Haptic Driver"] <--|REASSURE State Pulse| B
    end

    subgraph P2 ["Pillar 2: Mobile Core Engine"]
        B -->|Encrypted BLE Channel| C["Android Foreground Service"]
        D["Hardware Key Interrupts"] --> C
        C -->|Fused Location Provider| E["Deterministic GPS Telemetry"]
        C -->|Acoustic Ingestion| F["Gemini 1.5 Flash Audio Witness"]
        C -->|PSTN Fallback| G["Dual-Path SMS Broadcast"]
    end

    subgraph P3 ["Pillar 3: Cloud Dispatch Pipeline"]
        C -->|Firestore Real-Time Pipe| H["Cloud Coordination Engine"]
        H -->|Atomic Concurrency Lock| I[("Firestore Incident Store")]
        H -->|Push & Webhook Triggers| J["Caregiver Alert Gateways"]
    end

    subgraph P4 ["Pillar 4: Incident Command"]
        I -->|Vector Basemap Stream| L["Caregiver Operations Console"]
        L -->|Single-Responder Triage| M["Claim Incident / Route ETA"]
        M -->|Dispatch Reassurance| H
        H -->|Downlink Reassurance| C
        C -->|GATT Write Characteristic| K
    end

    style A fill:#0F172A,stroke:#38BDF8,stroke-width:2px,color:#F8FAFC
    style C fill:#0F172A,stroke:#38BDF8,stroke-width:2px,color:#F8FAFC
    style H fill:#0F172A,stroke:#F59E0B,stroke-width:2px,color:#F8FAFC
    style L fill:#0F172A,stroke:#10B981,stroke-width:2px,color:#F8FAFC
    style K fill:#0F172A,stroke:#22D3EE,stroke-width:2px,color:#F8FAFC
```

### Pillar 1: Embedded IoT Wearable (ESP32 & FreeRTOS)
* **Zero-Latency Physical Activation**: Custom mechanical switch wired to a hardware interrupt delivers sub-120ms transmission over Bluetooth Low Energy.
* **Closed-Loop Tactile Reassurance**: Incorporates an LRA/ERM vibration motor running custom haptic waveforms. When a caregiver claims an incident on the operations console, the wristband executes a rhythmic tactile pulse sequence (`REASSURE`), confirming rescue without audio cues.
* **Asynchronous State Machine**: Implemented on FreeRTOS with non-blocking timers, ensuring that BLE advertising and connection supervision intervals remain uninterrupted during vibration bursts.

### Pillar 2: Android Core Emergency Client (Kotlin & Jetpack Compose)
* **Independent Operation**: Operates fully autonomously without the wearable via a low-level physical Volume Key listener (triple-press detection) and TalkBack-certified accessibility semantics.
* **Foreground Lifecycle Resilience**: Bound to a `START_STICKY` Foreground Service with wakelock acquisition, persisting across process termination, deep-sleep battery optimizations, and system reboots.
* **Multi-Tiered Fused Location Engine**: Prioritizes fresh GPS fixes, falling back deterministically to Google Play Services cached locations and direct hardware NMEA providers.
* **Gemini 1.5 Flash Audio Witness**: Automatically records a 10-second high-fidelity ambient acoustic window upon activation, classifies environmental threats (impacts, distress calls, vehicular noise, ambient struggle), and presents structured intelligence to caregivers.
* **Blackout Stealth Mode**: Renders a zero-luminance pitch-black display to prevent hostile detection. De-escalation requires a tactile gesture ("V" shape stroke) verified by an internal geometric engine.

### Pillar 3: Cloud Coordination & Dispatch Pipeline (Firebase)
* **Zero-Trust Client Access**: No administrative keys bundled in client code. Uses restricted client tokens and Firebase App Check.
* **Transient Session Architecture**: Telemetry links automatically expire after 4 hours. All ephemeral coordinate records are scrubbed upon incident closure.
* **Atomic Concurrency Lock**: Eliminates bystander paralysis by enforcing a single-caregiver claim state machine on the incident document.

### Pillar 4: Incident Operations Command (WebGL Vector Web Portal)
* **Hardware-Accelerated Vector Basemaps**: Rendered using MapLibre GL with licensed CARTO Dark Matter vector tiles, providing continuous 60fps zooming, zero raster pixelation, and crisp typography.
* **Tactical Radar Telemetry**: Multi-stage crimson user beacon (`#EF4444`) with dual expanding radar rings, paired with an emerald caregiver navigation beacon (`#10B981`).
* **Live Proximity & Dynamic Routing**: Haversine distance calculations and moving animated trajectory lines towards the incident coordinate centroid.
* **Remote Reassurance Transmitter**: Allows caregivers to trigger downstream tactile reassurance pulses to the wristband with a single click.

---

## Architectural Comparison: Emergency Systems

| Architectural Dimension | Consumer Safety Apps | Legacy Telecare Pendants | KAAVAL Distributed Platform |
| :--- | :--- | :--- | :--- |
| **Activation Channel** | Touchscreen interaction | Stationary RF base station | **Hardware BLE Wearable + Physical Key Listener** |
| **Feedback Loop** | Unidirectional broadcast | Analog voice speakerphone | **Closed-Loop Bidirectional Tactile Pulses (`REASSURE`)** |
| **Situational Intelligence** | Unstructured text | Human operator audio | **Gemini 1.5 Flash Acoustic Scene Classification** |
| **Incident Coordination** | Generic group SMS | Proprietary call center | **WebGL Vector Command Map with Single-Claim Protocol** |
| **Network Resilience** | Data connection required | PSTN landline required | **Dual-Path Redundancy: Firestore over IP + GSM SMS** |
| **Discretion & Anti-Coercion** | Visible, audible alarms | Auditory siren/beeping | **Blackout Stealth Mode with Geometric Gesture Validation** |
| **Accessibility Standard** | Partial touch compliance | Physical hardware only | **W3C WCAG 2.1 Level AAA (~19.5:1 Contrast Ratio)** |

---

## Hardware Specifications & Wearable BOM

The KAAVAL Wearable prototype is optimized for low power, deterministic latency, and tactile clarity:

| Subsystem | Component Specification | Engineering Role |
| :--- | :--- | :--- |
| **Processing Core** | ESP32-WROOM-32 (Dual-Core Xtensa LX6 @ 240MHz) | FreeRTOS Task Scheduling, BLE Stack, Haptic Engine |
| **Tactile Trigger** | Momentary SPST Sealed Pushbutton | Low-travel, high-tactile mechanical interrupt |
| **Haptic Actuator** | Precision 1027 Coin ERM / LRA Actuator | Directional tactile confirmation & reassurance pulses |
| **Driver Circuit** | NPN 2N2222 with 1N4001 Flyback Protection | High-transient current isolation and back-EMF clamping |
| **Radio Link** | 2.4GHz BLE 4.2 (GATT Service `4fafc201...`) | Low-power telemetry and downstream control channel |
| **Power Management** | 3.7V 500mAh LiPo with Integrated TP4056 USB-C | Low-dropout regulation with deep-sleep current < 15µA |

---

## Repository Layout

```
KAAVAL/
├── android/                   # Native Android Core Engine
│   ├── app/src/main/java/     # Clean Architecture (MVVM, Hilt, Jetpack Compose)
│   │   ├── accessibility/     # High-contrast M3 theme, TalkBack semantics, Haptic waveforms
│   │   ├── ble/               # KaavalBleManager (GATT client, reconnect loop)
│   │   ├── data/              # Room Database, EmergencySession entity, Firestore repository
│   │   ├── service/           # EmergencyForegroundService (Sticky background lifecycle)
│   │   └── ui/                # Blackout Stealth UI, Emergency Countdown, Caregiver Setup
│   └── build.gradle.kts       # Android Gradle configuration (R8 ProGuard enabled)
│
├── firmware/                  # Embedded IoT Wearable
│   └── esp32_wristband/       # PlatformIO ESP32 Firmware
│       ├── src/main.cpp       # Asynchronous FreeRTOS haptic state machine & BLE server
│       └── platformio.ini     # Hardware toolchain definition
│
├── tracking-web/              # Caregiver Operations & Public Distribution
│   ├── live.html              # Tactical WebGL Vector Map (MapLibre + CARTO Dark Matter)
│   ├── download.html          # Public Android App Download & Dynamic QR Portal
│   └── firebase-config.js     # Secure zero-exposure fallback credentials
│
├── functions/                 # Cloud Coordination Functions (Node.js)
│   └── index.js               # SMS gateways, incident auto-expiration, and dispatch webhooks
│
├── docs/                      # Centralized Engineering Documentation
│   ├── architecture/          # System architecture, BLE specifications, and design tokens
│   ├── engineering/           # Sprint logs, adversarial test matrices, and voice/haptic guides
│   └── runbooks/              # Production launch, flashing, and verification procedures
│
└── firebase.json              # Firebase Hosting clean URLs, headers, and security rules
```

---

## Engineering Quickstart

### Prerequisites
- **Android Studio Jellyfish or later** (JDK 17)
- **Node.js 18+** & `firebase-tools` (`npm install -g firebase-tools`)
- **PlatformIO CLI** (`pip install platformio`)

### 1. Build Android Production Binary
```bash
cd android
./gradlew testDebugUnitTest       # Run test suite
./gradlew assembleRelease         # Compile R8-minified production APK (2.95 MB)
```

### 2. Flash Embedded Wearable Firmware
```bash
cd firmware/esp32_wristband
pio run --target upload           # Upload firmware via serial port
pio device monitor -b 115200      # Inspect GATT initialization logs
```

### 3. Local Web Simulation & Testing
```bash
cd tracking-web
python -m http.server 8080
# Access portal at http://localhost:8080/live.html?id=KVL-DEMO-TEST
```

### 4. Production Deployment
```bash
firebase deploy --only hosting,firestore:rules
```

---

## Security, Privacy & Ethics

* **Zero Persistent Geolocation History**: Coordinate records are deleted upon incident resolution and auto-expire after 4 hours.
* **Anti-Coercion Protocol**: Blackout UI prevents hostile actors from verifying active distress calls.
* **Cryptographic Data Minimization**: Ephemeral tracking sessions contain only precision metadata, battery telemetry, and audio scene classification labels.

---

## Program Credits & Acknowledgments

* **Kevin George** — *System Architecture, Mobile & AI Engineering*
* **Navami, Adwaid, Jewel** — *Hardware & Electrical Engineering (EEE)*
* **Hemang Mohan** — *Industry Project Mentor*

Developed with support from the **IEEE Sensors Council Industry Mentoring Program**.

---

<p align="center">
  <b>KAAVAL Ecosystem &copy; 2026</b><br>
  <i>Mission-critical emergency response infrastructure.</i>
</p>
