"""Charlotte's transparent, deterministic ethical decision support.

This is a policy aid, not consciousness, emotion, or a substitute for human review.
"""
from dataclasses import dataclass
from enum import Enum


class Decision(str, Enum):
    ALLOW = "allow"
    REVIEW = "review"
    DENY = "deny"


@dataclass(frozen=True)
class EthicalAssessment:
    decision: Decision
    reasons: tuple[str, ...]
    requires_human_approval: bool


def assess_action(
    *,
    authorized: bool,
    involves_harm: bool = False,
    privacy_risk: bool = False,
    consequential: bool = False,
    uncertain: bool = False,
) -> EthicalAssessment:
    """Conservatively assess an action from explicit, verified input signals.

    Callers must not treat unverified authorization as authorized. This module
    does not execute actions or override platform security and consent checks.
    """
    if not authorized:
        return EthicalAssessment(Decision.DENY, ("Authorization not verified.",), False)
    if involves_harm:
        return EthicalAssessment(Decision.DENY, ("Potential harm requires refusal or safer alternatives.",), False)
    reasons = []
    if privacy_risk:
        reasons.append("Privacy and consent require human review.")
    if consequential:
        reasons.append("Consequential action requires human approval.")
    if uncertain:
        reasons.append("Insufficient confidence; request clarification or review.")
    if reasons:
        return EthicalAssessment(Decision.REVIEW, tuple(reasons), True)
    return EthicalAssessment(Decision.ALLOW, ("No flagged concerns in supplied signals.",), False)
