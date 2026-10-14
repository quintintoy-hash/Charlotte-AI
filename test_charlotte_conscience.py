"""Basic regression checks for Charlotte's ethical decision support."""
from charlotte_conscience import Decision, assess_action


def test_unverified_authorization_denied():
    assert assess_action(authorized=False).decision is Decision.DENY


def test_harm_denied():
    assert assess_action(authorized=True, involves_harm=True).decision is Decision.DENY


def test_privacy_requires_review():
    result = assess_action(authorized=True, privacy_risk=True)
    assert result.decision is Decision.REVIEW
    assert result.requires_human_approval


def test_consequential_requires_review():
    assert assess_action(authorized=True, consequential=True).decision is Decision.REVIEW


def test_uncertainty_requires_review():
    assert assess_action(authorized=True, uncertain=True).decision is Decision.REVIEW


def test_unflagged_authorized_action_allowed():
    assert assess_action(authorized=True).decision is Decision.ALLOW
