# 🚀 KAAVAL — Step-by-Step Launch & Verification Runbook

This runbook provides the exact, production-verified commands to build, test, deploy, and verify all 4 pillars of the **KAAVAL Emergency Response Ecosystem**.

---

## 📋 Prerequisites

- **Host OS**: Windows 10/11 (PowerShell or Bash)
- **Root Directory**: `C:\Projects\KAAVAL`
- **Android SDK**: `platform-tools` (ADB) installed at `C:\Users\Asus\AppData\Local\Android\Sdk\platform-tools\`
- **Python**: 3.9+ for local web server
- **Arduino IDE / arduino-cli**: ESP32 by Espressif Systems board package installed

---

## 📱 1. Android Core Application (`android/`)

### 1.1 Run Automated Unit Tests
Validates Room database, Coroutines, State Machine, and SMS/Location handlers:
```powershell
cd c:\Projects\KAAVAL\android
.\gradlew testDebugUnitTest
```
*Expected Output*: `BUILD SUCCESSFUL in 3s` (29 actionable tasks passed).

### 1.2 Build Debug APK
Compiles Kotlin sources, generates Room DAOs, and packages APK:
```powershell
cd c:\Projects\KAAVAL\android
.\gradlew assembleDebug
```
*Output Artifact*: `android/app/build/outputs/apk/debug/app-debug.apk`

### 1.3 Install and Launch on Physical Device
Connect Android smartphone via USB with USB Debugging enabled:
```powershell
# Verify ADB connection
adb devices

# Stream-install APK over USB
adb install -r c:\Projects\KAAVAL\android\app\build\outputs\apk\debug\app-debug.apk

# Launch KAAVAL MainActivity
adb shell am start -n com.kaaval.app/.MainActivity
```

### 1.4 Live Logcat Diagnostics
Monitor foreground service lifecycle, BLE manager, and Firestore sync:
```powershell
adb logcat -v time -s KaavalEmergencyService:V FirebaseTracking:V KaavalBleManager:V
```

---

## 🌐 2. Caregiver Incident Command Portal (`tracking-web/`)

### 2.1 Start Local Web Server
Serve the web portal locally on port 8080:
```powershell
cd c:\Projects\KAAVAL\tracking-web
python -m http.server 8080
```

### 2.2 Access Live Portal
Open your browser to:
```text
http://localhost:8080/live.html?id=KVL-984321&lat=9.9312&lng=76.2673&name=Arjun%20Sharma&blood=O%2B
```

### 2.3 Local Firebase Configuration
For offline/local testing, ensure [firebase-config.js](file:///c:/Projects/KAAVAL/tracking-web/firebase-config.js) exists (untracked in Git, derived from `firebase-config.example.js`):
```powershell
cd c:\Projects\KAAVAL\tracking-web
if (-not (Test-Path "firebase-config.js")) {
    Copy-Item "firebase-config.example.js" "firebase-config.js"
}
```

---

## ⌚ 3. ESP32 Wearable Firmware (`wearable/KAAVAL_SOS/`)

### 3.1 Hardware Pinout
- **GPIO 4**: SOS Push Button (Active LOW, internal `INPUT_PULLUP`).
- **GPIO 5**: Haptic Vibration Driver (Active HIGH).

### 3.2 Flashing via Arduino IDE
1. Open [KAAVAL_SOS.ino](file:///c:/Projects/KAAVAL/wearable/KAAVAL_SOS/KAAVAL_SOS.ino) in Arduino IDE.
2. Select Board: **ESP32 Dev Module**.
3. Select Port: (e.g., `COM3`, `COM4`).
4. Click **Upload** (Ctrl + U).

### 3.3 Hardware Verification Matrix
- **Power On**: 150ms confirmation buzz.
- **BLE Connect**: 100ms friendly buzz on smartphone link.
- **3-Second SOS Hold**: 300ms confirmation buzz + sends `"SOS"` notification.
- **Unconnected SOS Press**: 600ms warning buzz.
- **Caregiver Reassurance**: Rhythmic double-heartbeat pulse (120ms high → 90ms low → 180ms high).

---

## ☁️ 4. Cloud & Security (`firestore.rules`, `firebase.json`)

### 4.1 Deploy Firestore Rules & Web Hosting
Deploy directly to the active Firebase project (`kaaval-94c1d`):
```powershell
cd c:\Projects\KAAVAL
firebase deploy --only hosting,firestore:rules
```

### 4.2 Verify Firestore Rule Integrity
Ensure read capabilities are time-limited to active session expiry and unguessable URLs:
```javascript
allow get: if resource == null || owner() || caregiver() || active();
allow list: if false; // Prevents enumeration
```

---

## 🔄 5. End-to-End Live Demonstration Workflow

1. **Trigger**:
   - Hold the ESP32 wristband button for 3 seconds (or hold the on-screen SOS button).
   - Wristband vibrates (300ms); phone speaks: *"Emergency activating in 5... 4... 3... 2... 1."*
2. **Audio Witness**:
   - Ambient speech is captured by `AudioWitnessManager.kt` and sent to Whisper + GPT-4o-mini.
   - Situation summary updates in Firestore `incidents/{id}`.
3. **Caregiver Command**:
   - Open `live.html` on laptop/phone.
   - Click **"I AM RESPONDING"**, choose ETA (e.g., *"6 mins"*), and click **Confirm**.
4. **Closed-Loop Reassurance**:
   - Phone speaks: *"Priya has acknowledged your alert. She is on her way, arriving in 6 minutes."*
   - Wristband pulses a double-heartbeat vibration on the user's wrist.
