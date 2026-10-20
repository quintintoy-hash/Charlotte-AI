# Charlotte Universal Device Migration — approved design requirement

Status: design specification only; not implemented or device-tested.

## Goal
Move Charlotte between compatible Android phones regardless of manufacturer, without losing the Creator identity or verified permitted data. Keep one combined final release package preference.

## Migration protocol
1. On old device, require authenticated Creator authorization to begin.
2. Create versioned, authenticated, encrypted export of eligible settings, preferences, conversation data and user-authorized files. Never export plaintext secrets, Android Keystore private keys, or raw biometric templates.
3. Verify export integrity and compatibility metadata before declaring backup successful.
4. On new device, install a trusted, signed Charlotte build through Android's permitted installation flow; require explicit user action for installation and runtime permissions.
5. Authenticate Creator through account recovery and a strong second factor. Re-enroll device-bound biometrics and provision new device-bound encryption keys.
6. Import only schema-validated, authenticated data; migrate versions transactionally with rollback and an audit record.
7. Detect supported Android capabilities, OS restrictions, battery/background policies, microphone/camera access and smart-home integrations. Report unsupported features without silently bypassing OS controls.
8. Run smoke tests for startup, persistence, settings, Guardian recovery and supported services.
9. Keep old phone intact until the Creator confirms new-device verification; only then offer optional old-device unlink/wipe after separate confirmation.

## Guardian integration
- Resume interrupted transfers safely and idempotently.
- Maintain a last-known-good encrypted backup with explicit retention and restore verification.
- Provide visible progress, recoverable error codes, and safe rollback.
- Never silently transfer ownership, remove the Creator, or deploy untested self-modifying code.

## Acceptance gates
- Test migration across at least two Android manufacturers and multiple supported OS versions.
- Test corrupt export, wrong credentials, lost connectivity, low storage, app version mismatch, interrupted import and rollback.
- Verify encrypted export, no raw biometric transfer, key rotation, permission re-consent, and preservation of the original device.
- Validate installation independently on physical devices. CI passing alone is insufficient.

## Current blocker
Samsung Galaxy A35 reports "App not installed"; obtain exact ADB installation error before claiming a working Android migration path.
