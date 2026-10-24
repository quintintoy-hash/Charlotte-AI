param(
    [Parameter(Mandatory = $true)]
    [string]$ApkPath,
    [string]$OutputDirectory = ".\\charlotte-install-diagnostics"
)
$ErrorActionPreference = "Stop"
if (-not (Test-Path -LiteralPath $ApkPath -PathType Leaf)) { throw "APK not found: $ApkPath" }
$adb = Get-Command adb -ErrorAction SilentlyContinue
if (-not $adb) { throw "ADB not found. Install Android SDK Platform Tools and add adb to PATH." }
New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$log = Join-Path $OutputDirectory "install-result.txt"
$devices = & adb devices -l 2>&1
$devices | Set-Content -Path (Join-Path $OutputDirectory "devices.txt")
$ready = @($devices | Where-Object { $_ -match '^[^\s]+\s+device\b' })
if ($ready.Count -ne 1) { throw "Expected exactly one authorised connected Android device. See devices.txt; unlock phone and accept USB debugging prompt." }
$serial = ($ready[0] -split '\s+')[0]
$info = @(
    "Model: $(& adb -s $serial shell getprop ro.product.model)"
    "Manufacturer: $(& adb -s $serial shell getprop ro.product.manufacturer)"
    "Android: $(& adb -s $serial shell getprop ro.build.version.release)"
    "SDK: $(& adb -s $serial shell getprop ro.build.version.sdk)"
    "ABI: $(& adb -s $serial shell getprop ro.product.cpu.abi)"
)
$info | Set-Content -Path (Join-Path $OutputDirectory "device-info.txt")
& adb -s $serial logcat -c 2>$null
$installOutput = & adb -s $serial install -r -- "$((Resolve-Path -LiteralPath $ApkPath).Path)" 2>&1
$installCode = $LASTEXITCODE
$installOutput | Set-Content -Path $log
& adb -s $serial logcat -d -t 1500 -v time 2>&1 |
    Select-String -Pattern 'PackageManager|PackageInstaller|INSTALL_FAILED|PackageParser|apksigner' |
    Set-Content -Path (Join-Path $OutputDirectory "package-log.txt")
Write-Host "Installation exit code: $installCode"
Write-Host "Diagnostics saved in $OutputDirectory"
Write-Host "Review files before sharing: device identifiers and package logs may contain sensitive data."
if ($installCode -ne 0 -or ($installOutput -join ' ') -match 'Failure \[') { exit 1 }
