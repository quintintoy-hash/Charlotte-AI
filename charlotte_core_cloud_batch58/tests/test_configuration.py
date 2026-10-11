from pathlib import Path


def test_pyproject_exists():
    assert (Path(__file__).resolve().parents[1] / 'pyproject.toml').is_file()


def test_androidx_enabled():
    repo_root = Path(__file__).resolve().parents[2]
    gradle_properties = repo_root / 'Charlotte_Android_Studio_Handoff' / 'gradle.properties'
    assert 'android.useAndroidX=true' in gradle_properties.read_text().splitlines()
