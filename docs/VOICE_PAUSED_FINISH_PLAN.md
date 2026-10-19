# Charlotte non-voice completion plan

Status: development checklist, not proof of implementation or testing.

## While voice testing is paused

- [ ] Verify Android CI and APK build; inspect install failure using ADB install output on Galaxy A35; confirm package ID, minSdk, signatures and APK integrity.
- [ ] Run Python and Android unit tests; fix reproducible failures and retain diagnostics.
- [ ] Audit approved normal home UI against canonical reference; do not replace approved artwork or reinstate removed fixed welcome header.
- [ ] Review optional Sentinel, Anakin and full-body modes as distinct opt-in modes; preserve normal appearance.
- [ ] Review text conversation handling, command parsing, errors, recovery and offline behavior.
- [ ] Implement or verify creator/secondary-user authorization, secure storage, consent gates, logs and safe cancellation.
- [ ] Review Help and Sentinel workflows for truthful device capability reporting, lawful integrations and fail-safe behavior; do not claim untested emergency dispatch.
- [ ] Review Settings for customizable greetings, email-address configuration (no credentials in source), integration permissions and revocation.
- [ ] Prepare repeatable release validation and versioned deliverables; never label a debug APK production-ready.

## Deferred voice test gate

- [ ] Microphone permission and capture on real phone.
- [ ] Wake word and always-listening constraints with clear listening indicator and stop controls.
- [ ] Speech recognition -> response generation -> British English TTS playback end-to-end.
- [ ] Real-device battery/background behavior and accessibility.

## Release gate

All critical security, install, build and device checks must pass before a release claim. Concept approval does not equal implemented functionality. Never store biometrics, private medical information, API keys or credentials in Git.
