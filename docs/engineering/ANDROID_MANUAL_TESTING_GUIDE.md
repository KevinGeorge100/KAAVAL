# 📱 Physical Android Device Manual Accessibility Testing Guide

**Document Version:** 1.0  
**Target Device:** Any physical Android Smartphone running Android 8.0+ (API 26+)  
**App Package:** `com.kaaval.app`  

---

## 🚀 Step 1: Installing KAAVAL onto Your Android Phone

### Option A: Via Android Studio (Recommended)
1. **Connect Phone**: Plug your Android phone into your computer via USB cable.
2. **Enable USB Debugging** on your phone:
   - Go to **Settings** ➔ **About Phone** ➔ Tap **Build Number** 7 times to enable **Developer Options**.
   - Go to **Settings** ➔ **System** ➔ **Developer Options** ➔ Enable **USB Debugging**.
   - Allow the USB Debugging permission prompt on your phone screen when plugged in.
3. **Run from Android Studio**:
   - Open Android Studio and choose **Open Project** ➔ Select `c:\Projects\KAAVAL\KAAVAL\android`.
   - Select your connected phone from the top device dropdown menu.
   - Click the green **Run 'app'** button (or press `Shift + F10`).

---

### Option B: Build APK & Install via ADB Command Line
1. Open PowerShell or Command Prompt in the project directory:
   ```powershell
   cd c:\Projects\KAAVAL\KAAVAL\android
   ```
2. Build the Debug APK:
   ```powershell
   .\gradlew assembleDebug
   ```
3. Install onto connected Android phone:
   ```powershell
   & "C:\Users\Asus\AppData\Local\Android\Sdk\platform-tools\adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
   ```

---

## ♿ Step 2: How to Test Accessibility Features

### 🧪 Test 1: Android TalkBack Screen Reader
1. **Enable TalkBack**:
   - Open **Settings** ➔ **Accessibility** ➔ **TalkBack** (or Accessibility Menu) ➔ Turn **ON**.
   - *(Tip: You can also hold Volume Up + Volume Down for 3 seconds on most Android phones to toggle TalkBack on/off).*
2. **Verify Screen Reader Output**:
   - **Touch the SOS Button**: TalkBack should speak:
     > *"Emergency SOS Button. Button. Double-tap to activate."*
   - **Touch Bottom Navigation Tabs**: TalkBack should speak:
     > *"Tab 1 of 4. Selected. SOS Home Tab."*
   - **Touch Contacts Tab**: TalkBack should speak:
     > *"Tab 2 of 4. Emergency Contacts Tab."*

---

### 🧪 Test 2: Voice Feedback Engine (Text-To-Speech)
1. **Turn Phone Media Volume Up**.
2. **Trigger SOS**:
   - Press and hold the yellow SOS button for **3 seconds**.
   - **Listen for spoken TTS announcements**:
     - 🔊 *"SOS button held. Starting emergency countdown."*
     - 🔊 *"Activating in 5 seconds..."*
     - 🔊 *"Activating in 4 seconds..."*
   - **Cancel Emergency**:
     - Tap **Cancel** during countdown:
     - 🔊 *"Emergency countdown cancelled."*

---

### 🧪 Test 3: Haptic Feedback Engine (Vibration)
1. Ensure vibration is enabled on your phone (**Settings** ➔ **Sound & Vibration** ➔ **Vibration ON**).
2. **Feel the Tactile Cues**:
   - **Touch SOS Button**: You will feel an instant **50ms tactile tick**.
   - **During Countdown**: You will feel **1 sharp pulse every second** (metronome tick).
   - **Tap Cancel**: You will feel a **descending double rumble vibration**.
   - **Activate SOS**: You will feel a **strong SOS vibration pattern** (`... --- ...`).

---

### 🧪 Test 4: High-Contrast Material 3 Visual Theme
1. **Observe Screen Design**:
   - Background is **Pure OLED Black** (`#000000`).
   - Primary SOS control is **Vibrant High-Contrast Yellow** (`#FFD600`).
   - Touch targets are large (min `48dp`).
   - Text is large, bold, and readable without squinting.

---

## 🔍 Summary Checklist

| Verification Item | Expected Behavior | Pass / Fail |
| :--- | :--- | :---: |
| **TalkBack Focus** | Focus ring highlights SOS button with clear speech announcement | [ ] Pass |
| **Voice TTS Speech** | Audio speaks clearly without overlapping | [ ] Pass |
| **Haptic Vibration** | Device vibrates distinctly on hold, tick, cancel, and activation | [ ] Pass |
| **High-Contrast Theme**| Pure black background with bright yellow controls | [ ] Pass |
