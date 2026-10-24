# Pipework Measurement Assistant — approved requirements

Status: APPROVED DESIGN, NOT IMPLEMENTED OR VALIDATED.

## Purpose
Support Boss, an experienced fitter, when recording site pipework measurements. Boss reports reading tape measurements and speaking them correctly, but sometimes transposing digits when writing them down and not recognising the written error on rereading. Avoid treating this as a lack of mechanical skill or automatically correcting ordinary conversation.

## Intended workflow
1. Boss identifies a pipe section, location, and measurement type, then speaks the measured value and units.
2. Charlotte captures the spoken measurement with speech recognition, shows the interpreted number clearly, and reads it back including units and pipe-section identifier.
3. Boss confirms or corrects the capture. Preserve the original transcript, confirmed value, timestamp, and revision history; do not silently alter measurements.
4. For fabrication/cutting, independently verify safety-critical dimensions against the tape, source drawing, photograph with legible reference, or a second measurement; do not treat voice readback alone as independent verification.
5. Flag suspected transposed digits, decimal errors, unit confusion, tolerance mismatches and missing verification, without guessing the intended measurement.
6. Allow offline-first use and later secure synchronisation subject to permissions.

## Future scope
Pipe-section log; measurement types (centre-to-centre, face-to-face, cut length, offset); isometric drawings; bend/offset calculations; material takeoffs; revision tracking; exportable job sheets. Calculations require validated formulas, units, tolerance conventions and engineering review.

## Interaction and safeguards
- Voice-first, hands-busy workflow with concise British-style readback; switch to precise, serious tone for critical dimensions.
- Require explicit confirmation for critical operations. Never autonomously approve cutting or fabrication.
- Clearly distinguish spoken values, recognised values, verified values, and calculated values.
- Speech recognition, microphone permissions, environmental noise and offline capability need implementation and physical-device testing.
- User controls recording, storage, retention, deletion, and export. Avoid unnecessary collection of site/client information.
- This document does not imply the feature works in the Android application today.
