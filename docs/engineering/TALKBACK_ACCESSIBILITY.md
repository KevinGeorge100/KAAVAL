# ♿ Android TalkBack Accessibility Specification

**Document Version:** 1.0  
**Target Audience:** Android Engineers, Accessibility Auditors, QA Testers  
**Application Package:** `com.kaaval.app`  

---

## 🎯 1. Overview & Accessibility Strategy

KAAVAL is built specifically for **visually impaired users**, which requires that every single user interface component operates seamlessly when navigated using **Android TalkBack** (Screen Reader). 

### Principles Applied:
1. **Explicit Component Naming**: Generic component labels (e.g. `"SOS"`, `"+"`, `"Cancel"`) are expanded into unambiguous accessibility titles (e.g. `"Emergency SOS Button"`, `"Add New Emergency Contact Button"`, `"Cancel Emergency Alert Button"`).
2. **Explicit Jetpack Compose Semantics Roles**: Every clickable element explicitly declares its semantic role (`Role.Button`, `Role.Tab`) so TalkBack announces action cues like *"Double tap to activate"*.
3. **Dynamic State Announcements (`stateDescription`)**: Interactive states change dynamically in real-time without visually distorting the UI (e.g., announcing remaining countdown seconds, active GPS tracking status, and primary contact flags).

---

## 📱 2. Semantics Implementation Matrix

### A. Main SOS Screen ([MainSosScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/MainSosScreen.kt))

| UI Element | Jetpack Compose Semantics | Content Description | State Description (`stateDescription`) |
| :--- | :--- | :--- | :--- |
| **Tactile SOS Trigger** | `role = Role.Button` | `"Emergency SOS Button"` | `"Ready. Double tap or press and hold to trigger emergency alert."` |
| **Countdown Container** | Header Container | `"Emergency Activation Countdown Timer"` | `"Activating emergency alert in {seconds} seconds. Double tap cancel emergency button below to stop."` |
| **Cancel Button** | `role = Role.Button` | `"Cancel Emergency Alert Button"` | `"Double tap to stop countdown and cancel alert"` |
| **Active Incident Card** | Incident Card | `"Live Caregiver Tracking Emergency Incident Status Card"` | `"Emergency Active. Incident ID: {id}. SMS sent to emergency contacts and primary contact call initiated."` |
| **I AM SAFE NOW Button**| `role = Role.Button` | `"Mark Self Safe and Resolve Emergency Button"` | `"Double tap to mark yourself safe and stop live location tracking"` |

---

### B. Emergency Contacts Screen ([ContactsScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/ContactsScreen.kt))

| UI Element | Jetpack Compose Semantics | Content Description | State Description (`stateDescription`) |
| :--- | :--- | :--- | :--- |
| **Add Contact Button** | `role = Role.Button` | `"Add New Emergency Contact Button"` | `"Double tap to open contact setup dialog"` |
| **Contact Card** | Card Container | `"Emergency Contact Card: {Name}"` | `"Relationship: {Relation}. Phone: {Number}. Primary Emergency Call Contact"` |
| **Primary Contact Badge**| Badge Container | `"Primary Call Contact Badge"` | `"Selected as primary emergency auto-dial contact"` |
| **Save Contact Button** | `role = Role.Button` | `"Save Emergency Contact Button"` | `"Double tap to save contact and close dialog"` |

---

### C. Medical Profile Screen ([MedicalProfileScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/MedicalProfileScreen.kt))

| UI Element | Jetpack Compose Semantics | Content Description | State Description (`stateDescription`) |
| :--- | :--- | :--- | :--- |
| **Medical Profile Card** | Card Container | `"Emergency Medical Profile Card for {Name}"` | `"Blood Group: {Group}. Known Allergies: {Allergies}. Current Medications: {Meds}. Emergency Notes: {Notes}"` |
| **Blood Group Badge** | Badge Container | `"Blood Group Badge"` | `"Blood Group {Group}"` |

---

### D. BLE Wearable Status Screen ([WearableStatusScreen.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/ui/screens/WearableStatusScreen.kt))

| UI Element | Jetpack Compose Semantics | Content Description | State Description (`stateDescription`) |
| :--- | :--- | :--- | :--- |
| **Device Status Card** | Card Container | `"Wearable Hardware Status Card for {Device}"` | `"Connection Status: Connected. Battery level: {Percentage} percent."` |
| **Battery Level Bar** | Progress Bar | `"Wearable Battery Level Indicator Bar"` | `"{Percentage} percent remaining"` |
| **Test Vibration Button**| `role = Role.Button` | `"Test Tactile Vibration Feedback Button"` | `"Double tap to trigger test vibration pulse pattern on phone and wearable"` |

---

### E. Bottom Navigation Bar ([MainActivity.kt](file:///c:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/MainActivity.kt))

| Tab Index | Jetpack Compose Semantics | Content Description | State Description (`stateDescription`) |
| :---: | :--- | :--- | :--- |
| **0** | `role = Role.Tab` | `"Emergency SOS Screen Tab"` | `"Selected. Tab 1 of 4"` or `"Not selected. Tab 1 of 4"` |
| **1** | `role = Role.Tab` | `"Emergency Contacts Screen Tab"` | `"Selected. Tab 2 of 4"` or `"Not selected. Tab 2 of 4"` |
| **2** | `role = Role.Tab` | `"Medical Profile Screen Tab"` | `"Selected. Tab 3 of 4"` or `"Not selected. Tab 3 of 4"` |
| **3** | `role = Role.Tab` | `"BLE Wearable Status Tab"` | `"Selected. Tab 4 of 4"` or `"Not selected. Tab 4 of 4"` |

---

## 🔍 3. Verification & Testing

To test and verify TalkBack operation:
1. Enable **TalkBack** on an Android device or emulator (`Settings > Accessibility > TalkBack > On`).
2. Swipe left/right to navigate through components and verify that TalkBack announces the explicit component name, state, and role.
3. Confirm that double-tapping any interactive element performs the expected action cleanly.
