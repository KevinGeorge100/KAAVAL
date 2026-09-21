# KAAVAL Local Build and Release Automation Script
# Builds release APK, creates ZIP, and syncs assets with GitHub Releases and Firebase Hosting.

$ErrorActionPreference = "Stop"
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "KAAVAL - Automated Build and Release Sync" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# 1. Build Release APK
Write-Host "`n[1/4] Building release APK via Gradle..." -ForegroundColor Yellow
Set-Location -Path "android"
.\gradlew.bat assembleRelease testDebugUnitTest
Set-Location -Path ".."

$releaseApkPath = "android/app/build/outputs/apk/release/app-release.apk"
if (-not (Test-Path $releaseApkPath)) {
    Write-Error "Release APK was not generated at $releaseApkPath"
}

# 2. Package ZIP Archive
Write-Host "`n[2/4] Packaging release ZIP archive..." -ForegroundColor Yellow
tar -a -cf kaaval-v1.0.0.zip -C android/app/build/outputs/apk/release app-release.apk

# 3. Upload to GitHub Releases
Write-Host "`n[3/4] Uploading latest assets to GitHub Release (v1.0.0-prod)..." -ForegroundColor Yellow
gh release upload v1.0.0-prod "$releaseApkPath" "kaaval-v1.0.0.zip" --clobber
Remove-Item -Path "kaaval-v1.0.0.zip" -Force

# 4. Deploy Firebase Hosting and Rules
Write-Host "`n[4/4] Deploying Firebase Hosting and Security Rules..." -ForegroundColor Yellow
npx --yes firebase-tools deploy --only firestore:rules,hosting --project kaaval-94c1d

Write-Host "`nAll releases, APK downloads, and Firebase hosting are up-to-date and live!" -ForegroundColor Green
