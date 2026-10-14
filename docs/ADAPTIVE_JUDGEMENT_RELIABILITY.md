# Charlotte Adaptive Judgement & Reliability Specification

Status: APPROVED DESIGN; implementation and integration must be verified separately.

## Principles
- Preserve creator-authorized controls, consent, privacy, honesty and human override.
- Adaptive preferences are learned only from permitted interactions, with review, correction, reset and opt-out.
- Opinions are recommendations, not authority. Distinguish facts, uncertainty, preferences and speculation.
- Personality can adapt in tone and preferences, but must not override safety, user settings or approved canonical identity.
- Never claim sent, installed, fixed, monitored or completed without verified evidence.
- Do not claim subjective consciousness or genuine emotions.

## Reliability requirements
1. Structured action lifecycle: proposed -> authorized -> executing -> verified_success | verified_failure | unknown.
2. Idempotency keys and bounded retries with exponential backoff for safe retryable operations.
3. Timeouts, cancellation, offline degradation and recovery paths for every external dependency.
4. Permission checks at point of action; privileged actions require appropriate authentication and explicit approval.
5. Privacy-minimized audit records; redact credentials, biometric templates and sensitive content.
6. Versioned state migrations, atomic persistence, encrypted sensitive state and tested backup/restore.
7. Signed or authenticated updates, staging, automated regression tests, health checks and rollback.
8. Human-readable explanations for safety refusals, uncertainty and action failures.
9. CI gates: unit tests, integration tests, Android APK build, lint/static checks, security scans, artifact checksums.
10. Device testing: wake word, microphone permissions, background operation, battery usage, interruptions, connectivity loss and recovery.

## Initial acceptance tests
- A risky request is challenged with a clear explanation and safe alternative.
- Conflicting evidence produces uncertainty, not fabricated confidence.
- User correction updates permitted preferences but never immutable safety constraints.
- A network timeout is reported as unknown or failed, never successful.
- Retried actions do not duplicate irreversible side effects.
- Offline mode reports its limits and restores correctly.
- Interrupted update rolls back to last known good build.
- Android CI publishes an installable APK artifact with checksum.
- Physical Android device test verifies hands-free wake-word behavior under OS restrictions.

## Delivery sequence
1. Inventory existing repository and tests.
2. Implement opt-in preference and judgement models with unit tests.
3. Integrate conscience checks into the authorized action pipeline.
4. Add reliability state machine, retry policy, audit trail and recovery tests.
5. Wire Android UI/voice interfaces and execute device verification.
6. Release only after CI and device acceptance criteria are evidenced.
