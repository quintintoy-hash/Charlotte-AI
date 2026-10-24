"""Charlotte Guardian: pure, dependency-free failover state machine.

This module does not restart Android services or deploy code. Host adapters must
perform those operations with platform permissions and explicit user approval.
"""
from dataclasses import dataclass
from enum import Enum
from typing import Optional


class GuardianState(str, Enum):
    PRIMARY = "primary"
    DEGRADED = "degraded"
    BACKUP = "backup"
    RECOVERING = "recovering"


@dataclass
class Guardian:
    failure_threshold: int = 3
    recovery_threshold: int = 2
    state: GuardianState = GuardianState.PRIMARY
    failures: int = 0
    successes: int = 0
    last_error: Optional[str] = None

    def __post_init__(self) -> None:
        if self.failure_threshold < 1 or self.recovery_threshold < 1:
            raise ValueError("thresholds must be positive")

    def heartbeat(self, healthy: bool, error: Optional[str] = None) -> GuardianState:
        """Record one observation. No external side effects."""
        if healthy:
            self.failures = 0
            self.successes += 1
            if self.state == GuardianState.DEGRADED:
                self.state = GuardianState.PRIMARY
            elif self.state == GuardianState.BACKUP and self.successes >= self.recovery_threshold:
                self.state = GuardianState.RECOVERING
            return self.state

        self.successes = 0
        self.failures += 1
        self.last_error = error or "health check failed"
        if self.state == GuardianState.RECOVERING:
            self.state = GuardianState.BACKUP
        elif self.failures >= self.failure_threshold:
            self.state = GuardianState.BACKUP
        elif self.state == GuardianState.PRIMARY:
            self.state = GuardianState.DEGRADED
        return self.state

    def confirm_recovery(self, passed: bool) -> GuardianState:
        """Promote only after the host verifies the primary runtime is usable."""
        if self.state != GuardianState.RECOVERING:
            raise RuntimeError("recovery confirmation requires recovering state")
        self.state = GuardianState.PRIMARY if passed else GuardianState.BACKUP
        self.failures = 0
        self.successes = 0
        if passed:
            self.last_error = None
        return self.state
