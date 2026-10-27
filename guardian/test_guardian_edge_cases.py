"""Additional regression tests for Guardian state transitions.

Run with: PYTHONPATH=guardian python -m unittest discover -s guardian -p 'test_*.py'
"""
import unittest

from guardian_core import Guardian, GuardianState


class GuardianEdgeCaseTests(unittest.TestCase):
    def test_transient_failure_resets_after_healthy_heartbeat(self):
        guardian = Guardian(failure_threshold=3)
        self.assertEqual(guardian.heartbeat(False, "temporary error"), GuardianState.DEGRADED)
        self.assertEqual(guardian.heartbeat(True), GuardianState.PRIMARY)
        self.assertEqual(guardian.failures, 0)
        self.assertEqual(guardian.heartbeat(False), GuardianState.DEGRADED)

    def test_recovery_cannot_be_confirmed_early(self):
        guardian = Guardian(failure_threshold=1, recovery_threshold=2)
        guardian.heartbeat(False)
        with self.assertRaises(RuntimeError):
            guardian.confirm_recovery(True)
        self.assertEqual(guardian.state, GuardianState.BACKUP)

    def test_success_does_not_bypass_required_recovery_confirmation(self):
        guardian = Guardian(failure_threshold=1, recovery_threshold=2)
        guardian.heartbeat(False)
        guardian.heartbeat(True)
        guardian.heartbeat(True)
        self.assertEqual(guardian.state, GuardianState.RECOVERING)
        guardian.heartbeat(True)
        self.assertEqual(guardian.state, GuardianState.RECOVERING)
        self.assertEqual(guardian.confirm_recovery(True), GuardianState.PRIMARY)

    def test_failed_recovery_requires_new_healthy_observations(self):
        guardian = Guardian(failure_threshold=1, recovery_threshold=2)
        guardian.heartbeat(False)
        guardian.heartbeat(True)
        guardian.heartbeat(True)
        guardian.confirm_recovery(False)
        self.assertEqual(guardian.state, GuardianState.BACKUP)
        self.assertEqual(guardian.successes, 0)
        self.assertEqual(guardian.heartbeat(True), GuardianState.BACKUP)
        self.assertEqual(guardian.heartbeat(True), GuardianState.RECOVERING)


if __name__ == "__main__":
    unittest.main()
