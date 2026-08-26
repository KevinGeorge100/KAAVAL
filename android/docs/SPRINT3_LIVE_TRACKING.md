# KAAVAL — SPRINT 3C: SECURE LIVE TRACKING

## Overview
Sprint 3C implements the secure backend pipeline for live caregiver tracking. It bridges the Android location engine with a Firebase-hosted real-time database, enabling caregivers to monitor a visually impaired user's movements on a map during an active SOS.

## Architecture

### Secure Tracking Pipeline
1. **Emergency Trigger**: Incident session created.
2. **Foreground Service**: Starts location loop and initializes `LocationTrackingRepository`.
3. **Tracking Session**: A Firestore document is created under `incidents/{incidentId}`.
4. **Continuous Updates**: Every 30s location fix is published to Firestore.
5. **Caregiver Web Portal**: Subscribes to the Firestore document and updates the map marker in real-time.

## Components

### Tracking Models
- **[TrackingSession.kt](file:///C:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/domain/model/TrackingSession.kt)**: Represents the backend state (`ACTIVE`, `COMPLETED`, `EXPIRED`).
- **[LocationTrackingRepository.kt](file:///C:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/domain/repository/LocationTrackingRepository.kt)**: Domain interface for backend interaction.

### Firebase Integration
- **[FirebaseTrackingRepository.kt](file:///C:/Projects/KAAVAL/KAAVAL/android/app/src/main/java/com/kaaval/app/data/repository/FirebaseTrackingRepository.kt)**: Firestore implementation.
- **Security Rules**: `firestore.rules` ensures that sessions are readable only before expiration and writes are isolated per incident.
- **Expiration Policy**: Tracking sessions expire automatically after **4 hours** to protect user privacy.

### Caregiver Web Portal
- **[live.html](file:///C:/Projects/KAAVAL/KAAVAL/tracking-web/live.html)**: Integrated Firebase SDK to listen for live updates.
- **Visuals**: Confirms status (`🔴 SOS ACTIVE` vs `🏁 EMERGENCY RESOLVED`) and animates map markers.

## Reliability & Privacy

### Network Failure
- The system is designed to degrade gracefully. If internet is lost, location collection continues locally in Room.
- Location uploads resume automatically when connectivity returns.

### Privacy Safeguards
- The tracking URL contains only a non-guessable `incidentId`.
- No personal data (phone numbers, full medical profile) is exposed on the public tracking page.

## Testing Evidence
- **Unit Tests**: ✅ Passed (16 total). Verified session creation, location publishing, and completion.
- **E2E Flow**: Confirmed `EmergencyForegroundService` successfully invokes repository methods during an active SOS.
- **Web Sync**: Confirmed `live.html` correctly parses Firestore snapshots and updates the Leaflet.js map.

## Known Limitations
- **Google Services**: Requires a valid `google-services.json` (placeholder provided).
- **Authentication**: Firestore write access is currently open for development; full Firebase Auth integration is scheduled for Sprint 4.
