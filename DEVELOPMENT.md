# Charlotte autonomous development rules

## Authority and limits
The project owner has authorised routine development, testing, code changes, and creation of reviewable pull requests. Do not purchase services, deploy to production, publish software, collect biometric data, delete personal data, or perform real-world device actions without explicit approval.

## Coding workflow
- Use the existing Batch 75 recovered source as baseline; do not invent missing modules.
- Make small, reviewable changes on branches.
- Run available tests and record commands, exit codes, failures and limitations.
- Prefer standard library/open source and avoid paid infrastructure unless explicitly approved.
- Never claim SAFE or production-ready merely because conceptual architecture was approved.
- Preserve Charlotte's approved visual design and safety/privacy requirements.
- Keep a changelog and restore checkpoints. Avoid committing build artifacts, secrets, biometric templates, or customer data.
- Android APK claims require actual successful Gradle builds; phone compatibility claims require device tests.
- Prepare a single final downloadable release archive only after real verification.

## First task
Import the extracted contents of Charlotte_Development_Pipeline_v2.zip. The archive currently exists in the ChatGPT conversation workspace, **not in this GitHub repository**. The agent cannot operate on files it cannot access.
