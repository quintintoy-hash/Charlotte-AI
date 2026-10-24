"""Durable Guardian state snapshots with atomic replacement and validation.

Snapshots are operational metadata only: never store tokens, biometric data,
conversation text, or other secrets here. Storage must be app-private.
"""
import json
import os
from pathlib import Path
from tempfile import NamedTemporaryFile

from guardian_core import Guardian, GuardianState

_VERSION = 1
_MAX_BYTES = 8192


def save_snapshot(guardian: Guardian, path: str) -> None:
    target = Path(path)
    target.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "version": _VERSION,
        "state": guardian.state.value,
        "failures": guardian.failures,
        "successes": guardian.successes,
        "failure_threshold": guardian.failure_threshold,
        "recovery_threshold": guardian.recovery_threshold,
    }
    temporary = None
    try:
        with NamedTemporaryFile(mode="w", encoding="utf-8", dir=target.parent,
                                prefix=".guardian-", delete=False) as stream:
            temporary = Path(stream.name)
            os.chmod(temporary, 0o600)
            json.dump(payload, stream, separators=(",", ":"))
            stream.flush()
            os.fsync(stream.fileno())
        os.replace(temporary, target)
    finally:
        if temporary is not None and temporary.exists():
            temporary.unlink()


def load_snapshot(path: str) -> Guardian:
    target = Path(path)
    if target.stat().st_size > _MAX_BYTES:
        raise ValueError("Guardian snapshot too large")
    with target.open("r", encoding="utf-8") as stream:
        data = json.load(stream)
    if not isinstance(data, dict) or data.get("version") != _VERSION:
        raise ValueError("Unsupported Guardian snapshot")
    expected = {"version", "state", "failures", "successes",
                "failure_threshold", "recovery_threshold"}
    if set(data) != expected:
        raise ValueError("Invalid Guardian snapshot fields")
    for key in ("failures", "successes", "failure_threshold", "recovery_threshold"):
        if type(data[key]) is not int or not 0 <= data[key] <= 1000000:
            raise ValueError("Invalid Guardian snapshot counter")
    guardian = Guardian(failure_threshold=data["failure_threshold"],
                        recovery_threshold=data["recovery_threshold"])
    guardian.state = GuardianState(data["state"])
    guardian.failures = data["failures"]
    guardian.successes = data["successes"]
    # Never trust a stale in-progress recovery after a process restart.
    if guardian.state == GuardianState.RECOVERING:
        guardian.state = GuardianState.BACKUP
        guardian.successes = 0
    return guardian
