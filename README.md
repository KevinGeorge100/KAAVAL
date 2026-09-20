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

### 🌐 Quick Access & Public Links

| Destination | Link | Description |
| :--- | :--- | :--- |
| 🚀 **Download Android App** | [kaaval-94c1d.web.app/download](https://kaaval-94c1d.web.app/download) | Production Release APK (2.95 MB) + 1-Tap QR Code |
| 📡 **Caregiver Command Map** | [kaaval-94c1d.web.app/live](https://kaaval-94c1d.web.app/live) | Real-time WebGL Vector Incident Operations Portal |
| 📦 **GitHub Releases** | [Releases v1.0.0-prod](https://github.com/KevinGeorge100/KAAVAL/releases/tag/v1.0.0-prod) | Signed APK binaries, release checksums, and change log |
| 📑 **Engineering Docs** | [docs/README.md](docs/README.md) | Technical architecture, hardware schematics, and runbooks |

---

## 👁️ Vision: Closing the Loop on Emergency Care

> **Our goal is not to build another generic panic button.**  
> We are building a **closed-loop emergency response ecosystem** tailored specifically for visually impaired individuals — from instantaneous, eyes-free distress activation to real-time caregiver coordination and bidirectional tactile reassurance until the individual is safe.

---

## 🚨 The Problem: The "Blind Panic" Paradox

Every existing emergency alert system makes a fatal assumption: **they assume the victim can see.**

```
   Existing Apps                                         KAAVAL
┌──────────────────────────────────────┐     ┌──────────────────────────────────────┐
│  • Requires unlocking phone screen   │     │  • Instant 1-click wristband trigger │
│  • Multi-step digital navigation     │     │  • Hardware volume key listener      │
│  • Fire-and-forget: User left in     │     │  • Bidirectional Haptic Reassurance  │
│    the dark with zero feedback       │     │    ("Help is coming!")               │
│  • Uncoordinated spam SMS to all     │     │  • Real-time Incident Command Map    │
│    contacts causing bystander delay  │     │    with Single-Claim triage protocol │
└──────────────────────────────────────┘     └──────────────────────────────────────┘
```

1. **Activation Failure**: Locating a phone, unlocking it, and tapping an on-screen button during an assault, medical crisis, or disorientation is impossible without sight.
2. **The Vacuum of Silence**: After pressing a generic SOS button, the victim stands in terrifying silence, having no idea whether their message was delivered, seen, or ignored.
3. **Caregiver Paralysis**: Family members receive frantic text messages simultaneously without coordination, resulting in duplicated calls, confusion, or assumption that "someone else is handling it."

---

## 🛡️ The KAAVAL Solution: 4 Integrated Pillars

KAAVAL transforms emergency response into a synchronized, 4-pillar closed loop:

```mermaid
flowchart TD
    subgraph P1 ["PILLAR 1: HARDWARE"]
        A["Wearable Tactile Wristband"] -->|BLE GATT Notification| B["Physical SOS Button"]
        K["Bidirectional Haptic Motor"] <--|REASSURE Command| B
    end

    subgraph P2 ["PILLAR 2: MOBILE ENGINE"]
        B -->|Encrypted BLE 4.2| C["Android Core Service"]
        D["Hardware Volume Keys"] --> C
        C -->|Foreground Service| E["Fused GPS Tracking"]
        C -->|Microphone Stream| F["Gemini 1.5 Flash Audio Witness"]
        C -->|Offline Fallback| G["Cellular SMS Broadcast"]
    end

    subgraph P3 ["PILLAR 3: CLOUD ENGINE"]
        C -->|Firestore Real-Time Stream| H["Cloud Dispatch Pipeline"]
        H -->|Auto-Expiring Session| I[("Firestore Incident DB")]
        H -->|Cloud Functions| J["Caregiver Push & SMS Gateways"]
    end

    subgraph P4 ["PILLAR 4: CAREGIVER PORTAL"]
        I -->|Vector Basemap Sync| L["Caregiver Command Portal"]
        L -->|1-Click Claim| M["Claim Emergency / Set ETA"]
        M -->|Send Tactile Pulse| H
        H -->|Remote Reassurance| C
        C -->|BLE Write Characteristic| K
    end

    style A fill:#EF4444,stroke:#FFFFFF,color:#FFFFFF
    style C fill:#1E293B,stroke:#3B82F6,color:#FFFFFF
    style H fill:#F59E0B,stroke:#FFFFFF,color:#000000
    style L fill:#10B981,stroke:#FFFFFF,color:#FFFFFF
    style K fill:#06B6D4,stroke:#FFFFFF,color:#000000
```

### 1. ⌚ Proprietary Tactile Wearable (Firmware: ESP32 / FreeRTOS)
- **Zero-Screen Trigger**: A dedicated physical tactile switch delivers instantaneous emergency dispatch without touching a smartphone.
- **Closed-Loop Tactile Reassurance**: When a caregiver acknowledges the alert on the web dashboard, the wristband vibrates with a rhythmic haptic heartbeat (`"HELP IS COMING"`), dispelling fear through touch.
- **Non-Blocking State Machine**: Built on FreeRTOS with asynchronous millisecond timers to prevent BLE disconnects during vibration cycles.

### 2. 📱 Android Core Emergency Client (Kotlin / Jetpack Compose)
- **Redundant Triggering**: Operates seamlessly standalone even without the wristband via triple-press Volume Key detection or TalkBack-optimized UI.
- **`START_STICKY` Foreground Service**: Persists through Android 14 aggressive battery killers, phone reboots, and app process kills.
- **3-Layer Location Fallback**: High-accuracy Fused Location → Cached GPS → Direct Hardware NMEA Provider.
- **Gemini 1.5 Flash Audio Witness**: Captures a 10-second high-fidelity ambient acoustic stream, classifying situational danger (cries for help, vehicular collision, footsteps, background distress) and transmitting real-time assessment to responders.
- **Blackout Stealth Mode**: Turns the phone display pitch-black during an active incident. Users verify their safety via a blind-friendly **"V" tactile gesture** to prevent hostile detection.

### 3. ☁️ Real-Time Cloud Coordination (Firebase)
- **Zero Exposure Architecture**: Eliminates hardcoded service keys in client bundles via environment fallbacks and Firebase App Check.
- **Automatic 4-Hour Session Expiration**: Enforces privacy by terminating tracking sessions after incident resolution.
- **Single-Caregiver Claim Protocol**: Prevents bystander confusion by allowing one primary caregiver to formally "Take Charge" with a live ETA.

### 4. 🛰️ Tactical Incident Operations Command (Web Portal)
- **Hardware-Accelerated Vector Basemaps**: Powered by MapLibre GL and licensed CARTO Dark Matter vector tiles for continuous 60fps smooth zooming and crisp vector typography.
- **High-Visibility Radar Beacons**: Multi-stage crimson user emergency beacon (`#EF4444`) with dual expanding radar waves and emerald caregiver navigation tracker (`#10B981`).
- **Live Proximity & Animated Route**: Dynamic Haversine calculation, estimated drive times, and illuminated neon cyan trajectory path.
- **Remote Tactile Pulse Transmitter**: Caregivers can click **"Send Tactile Pulse"** to send instant haptic reassurance vibrations straight to the victim's wristband.

---

## ⚡ Feature Comparison Matrix

| Capability | Generic SOS Apps | Traditional Medical Pendants | KAAVAL Ecosystem |
| :--- | :---: | :---: | :---: |
| **Eyes-Free Activation** | ❌ No (Touchscreen required) | ⚠️ Partial (Home base only) | ✅ **100% Eyes-Free (Wearable + Volume Keys)** |
| **Bidirectional Reassurance** | ❌ None (Fire-and-forget) | ⚠️ Voice only (Speakerphone) | ✅ **Haptic Pulse ("Help is on the way")** |
| **AI Situational Intelligence**| ❌ None | ❌ None | ✅ **Gemini 1.5 Flash Acoustic Witness** |
| **Live Caregiver Dispatch Map**| ⚠️ Static link via SMS | ❌ Proprietary Call Center | ✅ **Hardware-Accelerated Vector Web Portal** |
| **Offline Resilience** | ❌ Fails without data | ❌ Fails without cellular | ✅ **Dual-Path: Firestore + Offline SMS** |
| **Stealth & Anti-Coercion** | ❌ Loud screen alarm | ❌ Beeping alarm | ✅ **Blackout Mode + Gesture Confirmation** |
| **TalkBack / Accessibility** | ⚠️ Partial | ❌ None | ✅ **WCAG AAA Compliance (~19.5:1 Contrast)** |

---

## 🔬 Hardware Specifications & Wearable BOM

The physical KAAVAL Wristband is engineered for low power, resilience, and rapid tactile feedback:

| Component | Part / Specification | Purpose |
| :--- | :--- | :--- |
| **Microcontroller** | ESP32-WROOM-32 (Dual-Core 240MHz) | BLE 4.2 GATT Server & Non-blocking Haptics |
| **Tactile Trigger** | High-tactile SPST momentary switch | Instant mechanical emergency trigger |
| **Vibration Actuator**| Precision ERM / LRA Coin Motor | Rhythmic haptic reassurance pulses |
| **Driver Circuit** | NPN 2N2222 with 1N4001 Flyback Diode | Back-EMF suppression & safe GPIO switching |
| **Wireless Protocol**| BLE 4.2 (GATT Service `4fafc201...`) | Low-latency bi-directional smartphone telemetry |
| **Battery System** | 3.7V 500mAh LiPo + TP4056 USB-C Charger | All-day battery life with sleep optimization |

---

## 📁 Repository Directory Structure

```
KAAVAL/
├── android/                   # Android Core Application
│   ├── app/src/main/java/     # Kotlin Clean Architecture (MVVM + Hilt + Compose)
│   │   ├── accessibility/     # High-contrast M3 theme, TalkBack semantics, Haptic waveforms
│   │   ├── ble/               # KaavalBleManager (BLE GATT Client & reconnect loop)
│   │   ├── data/              # Room Local DB, EmergencySession, Firestore Repository
│   │   ├── service/           # EmergencyForegroundService (Sticky background lifecycle)
│   │   └── ui/                # Blackout Stealth UI, Emergency Countdown, Caregiver Setup
│   └── build.gradle.kts       # Android Gradle configuration (R8 ProGuard enabled)
│
├── firmware/                  # Embedded IoT Wearable
│   └── esp32_wristband/       # PlatformIO ESP32 Firmware
│       ├── src/main.cpp       # Asynchronous FreeRTOS haptic state machine & BLE GATT server
│       └── platformio.ini     # Hardware toolchain definition
│
├── tracking-web/              # Caregiver Incident Command & Public Portal
│   ├── live.html              # Tactical WebGL Vector Map (MapLibre + CARTO Dark Matter)
│   ├── download.html          # Public Android App Download & QR Scan Portal
│   └── firebase-config.js     # Secure zero-exposure fallback credentials
│
├── functions/                 # Firebase Cloud Functions (Node.js)
│   └── index.js               # SMS gateways, incident auto-expiration, and triage notifications
│
├── docs/                      # Comprehensive Documentation Hub
│   ├── architecture/          # System architecture, BLE specs, and design systems
│   ├── engineering/           # Sprint logs, testing guides, and voice/haptic engines
│   └── runbooks/              # Production launch and verification commands
│
└── firebase.json              # Firebase Hosting clean URLs, headers, and security rules
```

---

## 🛠️ Step-by-Step Developer Quickstart

### 1. Prerequisites
- **Android Studio Jellyfish or newer** (JDK 17)
- **Node.js 18+** & `firebase-tools` (`npm install -g firebase-tools`)
- **PlatformIO** (VS Code extension or CLI `pip install platformio`)

### 2. Clone the Repository
```bash
git clone https://github.com/KevinGeorge100/KAAVAL.git
cd KAAVAL
```

### 3. Build the Android Application
```bash
cd android
./gradlew testDebugUnitTest       # Run unit test suite
./gradlew assembleRelease         # Build production R8-minified APK (output in app/build/outputs/apk/release/)
```

### 4. Flash the ESP32 Wearable Firmware
```bash
cd ../firmware/esp32_wristband
pio run --target upload           # Flash firmware via USB-C
pio device monitor                # Open serial monitor at 115200 baud
```

### 5. Run the Caregiver Portal Locally
```bash
cd ../../tracking-web
python -m http.server 8080
# Open http://localhost:8080/live.html?id=KVL-DEMO-TEST
```

### 6. Deploy to Firebase
```bash
cd ..
firebase deploy --only hosting,firestore:rules
```

---

## 🔒 Security, Privacy & Ethics

- **Zero Permanent GPS History**: User coordinates are deleted upon incident resolution and auto-expire after 4 hours.
- **Zero Hardcoded Secrets**: Client repositories do not bundle unrestricted API keys.
- **Anti-Coercion Protocol**: Stealth Blackout Mode ensures attackers cannot see active emergency transmissions.
- **Accessible to All**: Full WCAG AAA color compliance (~19.5:1 contrast) and native screen reader compatibility.

---

## 👥 Team & Acknowledgments

- **Kevin George** — *Software Architect, Mobile & AI Lead*
- **Navami, Adwaid, Jewel** — *Hardware & Electrical Engineering Team (EEE)*
- **Hemang Mohan** — *Industry Project Mentor*

Special thanks to the **IEEE Sensors Council Industry Mentoring Program** for supporting the development and mentoring of KAAVAL.

---

<p align="center">
  <b>KAAVAL Ecosystem &copy; 2026</b><br>
  <i>Built with empathy, precision, and mission-critical engineering.</i>
</p>
