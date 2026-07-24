# Contributing to KAAVAL

KAAVAL is being developed incrementally against its approved scope and architecture documents in `docs/`.

## Before starting work

1. Confirm the work belongs to the approved MVP scope.
2. Read the relevant architecture and engineering documents.
3. Use a short-lived branch named `feature/s<no>-<description>`, `fix/s<no>-<description>`, `docs/<description>`, or `chore/<description>`.

## Pull requests

Each pull request must include:

- A concise description and link to the sprint task.
- Automated and manual test evidence.
- Accessibility impact, including TalkBack testing where a user flow changes.
- Privacy/security impact where data, permissions, tracking, or telemetry changes.

Never commit secrets, signing keys, real contact details, location data, tracking links, or production service credentials.

## Scope discipline

Do not add wearable, caregiver, AI, iOS, QR, medical-profile, push-notification, or escalation features without an approved, versioned scope change.
