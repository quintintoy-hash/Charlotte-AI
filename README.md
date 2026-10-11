# Charlotte AI — development repository

Private source repository for Charlotte, the user's permission-based personal AI assistant.

## Current status
- GitHub repository connected on 2026-10-11.
- Existing Batch 75 project and security archives recovered in the ChatGPT development workspace.
- **Recovered source is not yet imported into this repository.**
- Prior local Python test report: 110 passed. This is not an Android build verification.
- Do not claim production readiness or device verification.

## Development priorities
1. Import recovered source from Charlotte_Development_Pipeline_v2.zip, **extracting its contents** (do not commit only the ZIP).
2. Verify and reconcile the core, cloud, Android, and security components.
3. Run Python tests and Android Gradle tests/builds, fixing reproducible failures.
4. Build security controls, permission gates, and explicit user approvals.
5. Produce one final combined ZIP containing code, reports, and verified outputs.

No secrets, private biometric templates, credentials, or user health records belong in Git.
See DEVELOPMENT.md for the coding agent's rules.
