# Charlotte Samsung-first Android compatibility

Status: approved implementation requirements; not a claim of working device support.

## Target
- Primary physical validation device: Samsung Galaxy A35 (SM-A356E), Android 16 where available.
- Optimise supported behaviour for Samsung One UI without relying on undocumented APIs.
- Keep standard Android interfaces as the core architecture, with manufacturer-specific adapters behind capability checks.
- Preserve portability to Pixel, OnePlus and other Android phones.

## Android requirements
- Inspect actual APK signature, package ID, SDK metadata, ABI and install failure reason with adb before attributing failure to One UI.
- Explicitly request and explain microphone, notification, camera and foreground-service permissions when needed.
- Handle microphone and foreground service lifecycle restrictions without promising uninterrupted background listening.
- Surface battery optimisation and background restriction status as user-guided settings; do not silently bypass OS policy.
- Detect availability of speech recognition, TTS engines, biometric APIs and connected wearables.
- Keep voice and biometric testing deferred until the Creator can test; use deterministic mocks in CI.
- Do not assume Knox APIs are available to ordinary third-party apps.
- Support export/import of permitted settings and encrypted backups with re-authentication and new-device key provisioning.

## Test gates
1. CI: assemble APK, unit tests, verify signing and package metadata.
2. Samsung A35: capture exact adb install result, install, cold launch, background/foreground, restart and permission tests.
3. Samsung upgrade path: restore an authenticated backup on a second supported Samsung device, validate data integrity and permissions.
4. Cross-manufacturer regression: at least one non-Samsung Android device.
5. Guardian: failure detection, controlled restart, rollback, and truthful status; never silently install code.

## Release policy
- One final combined release download once acceptance gates are met.
- Interim debug artifacts are diagnostic, not final releases.
- No claim of compatibility until physical-device validation succeeds.
