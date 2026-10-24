import json
import tempfile
import unittest
from pathlib import Path

from guardian_core import Guardian, GuardianState
from guardian_persistence import load_snapshot, save_snapshot


class PersistenceTests(unittest.TestCase):
    def test_round_trip(self):
        with tempfile.TemporaryDirectory() as directory:
            path = str(Path(directory) / "guardian.json")
            original = Guardian(failure_threshold=2)
            original.heartbeat(False)
            original.heartbeat(False)
            save_snapshot(original, path)
            restored = load_snapshot(path)
            self.assertEqual(restored.state, GuardianState.BACKUP)
            self.assertEqual(restored.failures, 2)

    def test_in_progress_recovery_is_not_trusted_after_restart(self):
        with tempfile.TemporaryDirectory() as directory:
            path = str(Path(directory) / "guardian.json")
            guardian = Guardian(failure_threshold=1, recovery_threshold=1)
            guardian.heartbeat(False)
            guardian.heartbeat(True)
            self.assertEqual(guardian.state, GuardianState.RECOVERING)
            save_snapshot(guardian, path)
            self.assertEqual(load_snapshot(path).state, GuardianState.BACKUP)

    def test_rejects_malformed_snapshot(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "guardian.json"
            path.write_text(json.dumps({"version": 1, "state": "primary"}))
            with self.assertRaises(ValueError):
                load_snapshot(str(path))

    def test_rejects_boolean_counter(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "guardian.json"
            save_snapshot(Guardian(), str(path))
            data = json.loads(path.read_text())
            data["failures"] = True
            path.write_text(json.dumps(data))
            with self.assertRaises(ValueError):
                load_snapshot(str(path))


if __name__ == "__main__":
    unittest.main()
