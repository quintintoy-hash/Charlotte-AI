from pathlib import Path


def test_pyproject_exists():
    assert (Path(__file__).resolve().parents[1] / 'pyproject.toml').is_file()
