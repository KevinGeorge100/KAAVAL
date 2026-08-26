# KAAVAL — SPRINT 3B: REAL LOCATION ENGINE

## Overview
Sprint 3B implements the "Unbreakable Location" logic for KAAVAL. It ensures that coordinates are acquired reliably, fallbacks are used when GPS is weak, and tracking continues in the background independently of the UI.

## Architecture

### Fused Location Provider
- **FusedLocationProviderClient**: Used as the primary source for coordinates. It combines GPS, Wi-Fi, and Cell signals.
- **LocationManager**: A clean abstraction that manages fresh GPS locks and fallback logic. It provides both one-shot (`getCurrentLocation`) and continuous (`startLocationUpdates`) methods.

### Foreground Service Ownership
- **EmergencyForegroundService**: Owns the Android runtime location loop. It maintains the active `EmergencySession` and persists coordinates to the database.
- **Lifecycle**: Location updates start when the service is started with an `incidentId` and stop when the service is destroyed (session complete).

### Location Update Policy
- **Target Interval**: 30 seconds.
- **Fastest Interval**: 15 seconds.
- **Accuracy**: `Priority.PRIORITY_HIGH_ACCURACY`.
- *Note: These are development values and can be tuned later for battery efficiency.*

## Reliability & Fallbacks

### Multi-Stage Fallback
1. **Fresh GPS Lock**: 10-second high-accuracy attempt.
2. **Last Known (Fused)**: Immediate check of Google's cached location.
3. **Direct Android API**: Final fallback via `GPS_PROVIDER` or `NETWORK_PROVIDER` directly from the OS.

### GPS Timeout
- If a fresh GPS fix takes longer than 10 seconds, the system automatically uses the most recent stale coordinate to avoid blocking the emergency dispatch.

### Accuracy Handling
- Every coordinate includes an **Accuracy (meters)** value and **Timestamp**.
- Stale locations are clearly marked by their original timestamps.

## Android 14+ Implementation

### Permissions
- **ACCESS_FINE_LOCATION**: Required for the High Accuracy requirement.
- **ACCESS_COARSE_LOCATION**: Required as a baseline.
- **FOREGROUND_SERVICE_LOCATION**: Mandatory for tracking while the app is in the background.
- *Note: ACCESS_BACKGROUND_LOCATION is not used yet as the Foreground Service handles background persistence.*

### Background Behavior
- Tracking survives the user pressing the Home button or the system killing the `MainActivity`.
- The `EmergencyForegroundService` is marked as `START_STICKY` for OS resilience.

## Future Integration Points
- **Backend Dispatch**: The `EmergencyForegroundService` is the exact point where a "Location Pusher" should be added to sync coordinates with a remote Firebase database.
- **Battery Optimization**: A toggle can be added to the `LocationUpdatePolicy` to slow down updates if the `BatteryGuardian` detects low power.

## Status
- **Build**: ✅ Passed
- **Unit Tests**: ✅ Passed (9 total)
- **Reboot Survival**: ❌ NOT IMPLEMENTED — FUTURE RELIABILITY TASK
