# Listen Mode — Speech and Emotional Cues Analysis (SAFE design specification)

Status: approved design only; not implemented, validated, or clinically reliable.

## Activation and consent
- "Listen" activates a clearly indicated, explicitly permissioned session for transcription and notes. "Listen off" immediately stops capture, analysis and any scheduled retry.
- Emotional-cue analysis is a separate opt-in toggle, OFF by default, and only used when all relevant participants have been informed and applicable consent and recording laws permit.
- Never secretly record or infer private attributes; offer on-device processing where possible and configurable retention/deletion.
- During Listen Mode, Charlotte remains silent unless a separately configured emergency safety response is justified.

## Permitted analysis
- Describe observable features such as pitch, speaking rate, volume changes, pauses, speech tremor, and verbal statements, with timestamps and quality caveats.
- May offer cautious hypotheses such as "speech sounds strained" or "speaker explicitly said they are afraid", but must never present emotion as established from voice alone.
- Compare statements for text-level inconsistencies, while allowing for misunderstanding, memory errors, language differences, disability, noise and context.

## Prohibited conclusions
- No voice-based lie detector, deception score, truthfulness verdict, accusation, guilt finding, or claim to read minds.
- No reliable determination of fear, danger, mental-health condition, or intent solely from vocal characteristics.
- No biometric identity recognition or emotion profiling of bystanders without separate lawful consent.
- No automated punitive, security or law-enforcement action based on these inferences.

## Output and validation
- Distinguish transcript, direct quote, observed audio cue, tentative interpretation, alternative explanations and unknowns.
- Show uncertainty without invented numerical confidence. Abstain on noisy, short, overlapping or insufficient recordings.
- User can inspect, correct, delete and disable analyses. Record access and errors securely.
- Test with consented, diverse accents, languages, noisy conditions and non-native speech; audit false positives and bias before any release.
