import unittest
from guardian_core import Guardian, GuardianState


class GuardianTests(unittest.TestCase):
    def test_degrades_and_fails_over(self):
        g = Guardian()
        self.assertEqual(g.heartbeat(False), GuardianState.DEGRADED)
        self.assertEqual(g.heartbeat(False), GuardianState.DEGRADED)
        self.assertEqual(g.heartbeat(False), GuardianState.BACKUP)

    def test_recovers_only_after_confirmation(self):
        g = Guardian(failure_threshold=1)
        g.heartbeat(False)
        self.assertEqual(g.heartbeat(True), GuardianState.BACKUP)
        self.assertEqual(g.heartbeat(True), GuardianState.RECOVERING)
        self.assertEqual(g.confirm_recovery(True), GuardianState.PRIMARY)

    def test_failed_recovery_returns_to_backup(self):
        g = Guardian(failure_threshold=1, recovery_threshold=1)
        g.heartbeat(False)
        g.heartbeat(True)
        self.assertEqual(g.confirm_recovery(False), GuardianState.BACKUP)

    def test_unhealthy_recovery_reverts_to_backup(self):
        g = Guardian(failure_threshold=1, recovery_threshold=1)
        g.heartbeat(False)
        g.heartbeat(True)
        self.assertEqual(g.heartbeat(False), GuardianState.BACKUP)

    def test_bad_thresholds(self):
        with self.assertRaises(ValueError):
            Guardian(failure_threshold=0)


if __name__ == "__main__":
    unittest.main()
