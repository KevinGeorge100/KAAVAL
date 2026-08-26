# KAAVAL — SPRINT 3A: PERSISTENT EMERGENCY SESSION FOUNDATION

## Overview
This sprint establishes the transition from a one-off "SOS event" to a persistent "Emergency Session". This foundation ensures that tracking and emergency logic survive app closure, activity destruction, and phone movement.

## Key Concepts

### EmergencyState vs EmergencySession
- **EmergencyState**: Represents the granular, short-lived lifecycle of the emergency (e.g., `Countdown`, `AcquiringLocation`, `LiveTracking`). It is managed by the `EmergencyStateManager`.
- **EmergencySession**: Represents the persistent identity of the incident. It contains the `incidentId`, start time, status, and last known coordinates. It is stored in the database.

## Architecture

### Session Ownership
- **EmergencyForegroundService**: Owns the execution of an active session. It is started with an `incidentId` and remains in the foreground until the session is completed or cancelled.
- **KaavalRepository**: Acts as the `EmergencySessionRepository` implementation, managing the Room database storage.

### Recovery Behavior
- When `MainActivity` is created, the `EmergencyViewModel` observes the active session from the repository.
- If a session is found in `ACTIVE` or `COMPLETING` status, the UI automatically transitions to the corresponding emergency screen.

### Single Active Session Rule
- Only one emergency session can be active at a time.
- If a trigger occurs while a session exists, the new activation is rejected and logged as `DUPLICATE_SESSION_REJECTED`.

## Android 14+ Compatibility
- **Service Type**: `FOREGROUND_SERVICE_TYPE_LOCATION` is required for continuous tracking.
- **Permissions**: `FOREGROUND_SERVICE_LOCATION` must be declared in the manifest.
- **Background Restrictions**: Starting a foreground service from the background is restricted. Future BLE/Lock-screen triggers must use an approved path (e.g., PendingIntent or AccessibilityService).

## Implementation Details

### Files Created
- `EmergencySession.kt`: Domain model and status enums.
- `EmergencySessionRepository.kt`: Abstraction for session persistence.
- `EmergencySessionEntity.kt`: Room entity for sessions.
- `EmergencySessionDao.kt`: Database access for sessions.
- `EmergencySessionTest.kt`: Unit tests for session logic.

### Files Modified
- `KaavalDatabase.kt`: Added session entity and DAO.
- `KaavalRepository.kt`: Implemented the session repository abstraction.
- `EmergencyStateManager.kt`: Updated to manage `incidentId` and emit `StartSession` actions.
- `EmergencyActionDispatcher.kt`: Updated to handle session creation and service management.
- `EmergencyForegroundService.kt`: Refactored to accept `incidentId` and log start/stop events.
- `EmergencyViewModel.kt`: Implemented recovery logic and decoupled from the long-running loop.
- `MainActivity.kt`: Updated factory initialization.

## Future Integration Points
- **GPS**: The `EmergencyForegroundService` will host the periodic GPS request loop.
- **BLE**: Hardware triggers will start the service directly.
- **Caregiver Response**: `SmsReplyReceiver` will update the active session status in the repository.
