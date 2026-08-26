# KAAVAL — POST-AUDIT ARCHITECTURE REVIEW

**Date**: August 10, 2026
**Subject**: Transition from Event-Driven SOS to Persistent Emergency Sessions

---

## 1. Current Architecture
*   **Trigger**: Volume UP x3, "P" Gesture, or Long-Press (UI).
*   **Ownership**: `MainActivity` / `EmergencyViewModel` owns the `EmergencyStateManager`.
*   **State Machine**: Reacts to events (Trigger -> Countdown -> Activated).
*   **Execution**: `EmergencyActionDispatcher` delegates one-time tasks (Send SMS, Start Call).
*   **Backgrounding**: `EmergencyForegroundService` starts but acts only as a "Keep-Alive" notification. It does not perform work.
*   **Persistence**: Room database records the state, but recovery is manual/UI-triggered.

## 2. Identified Weaknesses
1.  **Process Vulnerability**: If the OS kills the app under memory pressure, the "Active" SOS logic stops because it lives in the Activity's ViewModel.
2.  **Ghost Service**: The foreground service satisfies the Android requirement for background execution but lacks the "doing" logic (GPS/Dispatch).
3.  **One-Shot Dispatch**: SMS/Telegram alerts are sent once. If the user moves, caregivers have stale information.
4.  **Interaction Trap**: The app is unreachable from the lock screen.
5.  **Single Point of Failure**: No escalation if the primary caregiver doesn't acknowledge the alert.

## 3. Proposed Emergency Session Architecture
We will move from "Firing an SOS" to "Managing a Session".

*   **Session ID**: A unique `incidentId` generated at the start of the countdown.
*   **Owner**: `EmergencyForegroundService` becomes the **Source of Truth**.
*   **Lifecycle**:
    1.  **Creation**: Trigger received -> Service Starts -> Session ID generated.
    2.  **Persistence**: Every state change (Acquiring, Sending, Tracking) is written to `EmergencyStateEntity` in Room immediately.
    3.  **Resilience**: On process recreation, the Service checks the DB. If an "Active" session exists, it resumes execution (GPS/Pusher) immediately.
    4.  **UI Sync**: `MainActivity` binds to the Service or observes the Repository Flow to show the state. If the Activity dies, the Service continues.

## 4. Foreground Service Responsibility
The `EmergencyForegroundService` will transition from a notification wrapper to an **Orchestrator**:
*   Manage the `EmergencyStateManager` instance.
*   Run the **Continuous Location Loop**.
*   Handle **Caregiver Acknowledgement** via the `SmsReplyReceiver`.
*   **Type**: `FOREGROUND_SERVICE_TYPE_LOCATION`.
*   **Restrictions**: Android 14+ requires `FOREGROUND_SERVICE_LOCATION` permission and a valid `id` for the notification. Service must be started within a short window of the trigger.

## 5. Location Architecture
*   **Standard Interval**: 60 seconds (Balance of battery/accuracy).
*   **Emergency Interval**: 15 seconds (During first 10 minutes of active SOS).
*   **Fallback**:
    1.  Fused Location (GPS + Network).
    2.  `lastLocation` cache.
    3.  Direct `GPS_PROVIDER` access.
*   **Network Loss**: If offline, location updates are saved to Room with a `isSynced = false` flag and pushed in bulk when connectivity returns.

## 6. Caregiver Acknowledgement Model
Success is no longer defined by "SMS Sent."
1.  **ALERT_SENT**: Dispatcher confirms SMS/Telegram sent.
2.  **CONTACT_NOTIFIED**: (Optional) SMS Delivery Report received.
3.  **ACKNOWLEDGED**: `SmsReplyReceiver` detects "OK/COMING" from a registered number.
4.  **RESPONSE_ACTIVE**: UI and Haptics change to "Help is on the way" mode.
5.  **ESCALATION**: If no ACK within 3 minutes, the system auto-dials the next contact in the list.

## 7. Lock-Screen Trigger Options
*   **Legitimate Physical Buttons**: `onKeyDown` works ONLY in foreground.
*   **Media Buttons**: Requires a fake "Silent Media" session. Unreliable on modern Android.
*   **Accessibility Service**: The most robust way to intercept volume keys globally. *Risk*: High friction setup for users.
*   **Wearable/BLE**: `KaavalBleManager` can trigger while the phone is locked if the service is running. This is our **safest physical path**.
*   **Accessibility Shortcut**: Android's built-in "Press both volume keys" shortcut to launch KAAVAL.

## 8. Live Tracking Architecture
*   **Backend**: Use Firebase Realtime Database or Firestore under `/incidents/{incidentId}/locations`.
*   **Transport**: Service pushes JSON payload: `{lat, lon, accuracy, timestamp, battery}`.
*   **Caregiver View**: The `/live/{incidentId}` web-link polls this location data and renders it on a map.

## 9. Android Platform Constraints
*   **Background Start**: Activity cannot be started from the background (Android 10+). We must use **High-Priority Notifications** or **Full-Screen Intents**.
*   **Battery Optimizations**: Users must be guided to disable "Battery Optimization" for KAAVAL to ensure the Service isn't killed.
*   **Permission**: `ACCESS_BACKGROUND_LOCATION` is mandatory for tracking when the app is closed.

## 10. Recommended Implementation Order
1.  **State Migration**: Move the `EmergencyStateManager` into a Singleton or the Foreground Service.
2.  **Location Loop**: Implement a 30s tick inside `EmergencyForegroundService` that updates the `EmergencyState` with new coordinates.
3.  **Repository Sync**: Update `KaavalRepository` to be the bridge between Service and Activity.
4.  **Caregiver Cycling**: Update `SmsActionHandler` to track call success and move to the next contact.
5.  **Lock-screen Recovery**: Implement a "Full Screen Intent" for the SOS screen to appear over the lock screen when triggered by BLE/Volume.

## Risks
*   **Privacy**: Permanent tracking is a risk; session must have an absolute timeout (e.g., 4 hours).
*   **Data Usage**: Continuous GPS/Data push will drain the battery of a visually impaired student significantly.

## Decisions Required
*   **Escalation Policy**: How long do we wait for a caregiver to answer before calling the next one? (Suggest 45 seconds).
*   **Tracking Transport**: Should we implement a real Firebase backend now or continue with the "Simulated" link?

---

### Safest Next Implementation Step:
**Migrate the `EmergencyStateManager` and Location logic into the `EmergencyForegroundService`.** 
This ensures the SOS doesn't die when the user switches apps or locks their phone, which is the most critical safety failure identified in the audit.
