#!/usr/bin/env python3
"""Static Android packaging checks; does not certify APK installation."""
from pathlib import Path
import re
import sys
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
app = root / "app"
gradle = (app / "build.gradle.kts").read_text(encoding="utf-8")
manifest = ET.parse(app / "src/main/AndroidManifest.xml").getroot()
android = "{http://schemas.android.com/apk/res/android}"
errors = []

def check(condition, message):
    if not condition:
        errors.append(message)

def setting(name):
    match = re.search(r"(?m)^\s*" + re.escape(name) + r"\s*=\s*(\d+|\"[^\"]+\")", gradle)
    return match.group(1).strip('"') if match else None

app_id = re.search(r'(?m)^\s*applicationId\s*=\s*"([^"]+)"', gradle)
check(bool(app_id), "applicationId missing")
check(setting("minSdk") is not None, "minSdk missing")
check(setting("targetSdk") is not None, "targetSdk missing")
check(setting("versionCode") is not None, "versionCode missing")
check(setting("versionName") is not None, "versionName missing")
application = manifest.find("application")
check(application is not None, "application element missing")
if application is not None:
    check(application.get(android + "allowBackup") == "false", "Backup must be explicitly disabled pending privacy review")
    check(application.get(android + "usesCleartextTraffic") == "false", "Cleartext traffic must be disabled")
    activities = application.findall("activity")
    launcher = [a for a in activities if any(
        f.find("action") is not None and any(
            x.get(android + "name") == "android.intent.action.MAIN"
            for x in f.findall("action"))
        for f in a.findall("intent-filter"))]
    check(len(launcher) == 1, "Expected exactly one MAIN launcher activity")
    if launcher:
        check(launcher[0].get(android + "exported") == "true", "Launcher must be exported")
permissions = {p.get(android + "name") for p in manifest.findall("uses-permission")}
check("android.permission.INTERNET" in permissions, "Internet permission missing")
print("Application ID:", app_id.group(1) if app_id else "MISSING")
for key in ("minSdk", "targetSdk", "versionCode", "versionName"):
    print(key + ":", setting(key))
if errors:
    for error in errors:
        print("FAIL:", error, file=sys.stderr)
    sys.exit(1)
print("PASS: static Android package configuration checks (not device installation verification)")
