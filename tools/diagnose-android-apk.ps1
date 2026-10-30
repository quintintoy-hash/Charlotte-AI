# Charlotte APK diagnostics (Windows PowerShell)
# Usage: .\tools\diagnose-android-apk.ps1 -ApkPath .\app-debug.apk
# Optional device installation test: add -Install
# Never upload reports without reviewing them: device IDs can be personal data.
[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$ApkPath,
    [switch]$Install
)
$ErrorActionPreference = "Stop"
try {
    $apk = (Resolve-Path -LiteralPath $ApkPath).Path
    if (-not [IO.File]::Exists($apk)) { throw "APK not found: $ApkPath" }
    $item = Get-Item -LiteralPath $apk
    Write-Host "APK: $($item.Name)"
    Write-Host "Size (bytes): $($item.Length)"
    Write-Host "SHA256: $((Get-FileHash -LiteralPath $apk -Algorithm SHA256).Hash)"

    Add-Type -AssemblyName System.IO.Compression
    $zip = [IO.Compression.ZipFile]::OpenRead($apk)
    try {
        $entries = @($zip.Entries | ForEach-Object { $_.FullName })
        foreach ($required in @("AndroidManifest.xml", "classes.dex")) {
            if ($required -notin $entries) {
                Write-Warning "Missing typical APK entry: $required (DEX may be split or named classes2.dex etc.)"
            }
        }
        $signature = @($entries | Where-Object { $_ -match '^META-INF/.*\.(RSA|DSA|EC)$' })
        Write-Host "V1 signature files: $($signature.Count) (0 may be normal for APK Signature Scheme v2/v3)"
        Write-Host "Archive entries: $($entries.Count)"
    } finally { $zip.Dispose() }

    $adb = Get-Command adb -ErrorAction SilentlyContinue
    if (-not $adb) {
        Write-Warning "ADB not found on PATH. Install Android SDK Platform Tools for device diagnostics."
        if ($Install) { throw "Cannot install without adb." }
        return
    }
    Write-Host "--- ADB devices ---"
    & $adb.Source devices -l
    if ($LASTEXITCODE -ne 0) { throw "adb devices failed." }
    $devices = @(& $adb.Source devices | Select-String '\sdevice$')
    if ($devices.Count -ne 1) {
        Write-Warning "Expected exactly one authorized connected device; found $($devices.Count)."
        if ($Install) { throw "Refusing install without exactly one authorized device." }
    } else {
        Write-Host "--- Device Android version ---"
        & $adb.Source shell getprop ro.build.version.release
        Write-Host "--- Device ABI ---"
        & $adb.Source shell getprop ro.product.cpu.abilist
    }
    if ($Install) {
        Write-Host "--- Attempting installation (explicit -Install requested) ---"
        & $adb.Source install -r $apk
        $exit = $LASTEXITCODE
        if ($exit -ne 0) { throw "ADB installation failed (exit code $exit). Look for INSTALL_FAILED_* above." }
        Write-Host "ADB reported installation success. Launch and runtime behavior are NOT verified."
    } else {
        Write-Host "No installation attempted. Add -Install to explicitly test the installation."
    }
} catch {
    Write-Error $_.Exception.Message
    exit 1
}
