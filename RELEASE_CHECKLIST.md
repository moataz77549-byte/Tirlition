# Rateel Beta 1 release gate

A check is marked only after evidence is recorded. None below is waived by a successful compile.

## Repository and CI
- [ ] Stage-4 code reviewed and pushed to the canonical remote
- [ ] Clean working tree and local HEAD equals remote HEAD
- [ ] All unit tests, instrumented tests, lint and debug/release builds pass
- [ ] Final CI green and full commit SHA recorded

## Sources and rights
- [ ] Current MP3Quran and Qurango responses parsed; ten radio endpoints exercised
- [ ] Reciters, full and partial mushafs, and sample surahs exercised
- [ ] Makkah/Madinah status honestly represented
- [ ] Recording/download rights checked in UI and domain/worker; attribution displayed
- [ ] No fake production source or secret in Git/APK

## Playback and downloads
- [ ] Radio, surah, local recording and downloaded surah play on actual device
- [ ] Background, notification, lock, Bluetooth, focus and noisy handling checked
- [ ] Recording from a source with explicit permission: free/timed, local replay and cleanup
- [ ] Single/batch/full download, progress, pause/resume/cancel/retry, Wi-Fi only
- [ ] Offline playback, corruption/missing file, delete, storage usage and Room upgrades

## UX and Android
- [ ] Original adaptive/monochrome/legacy icon and splash installed
- [ ] RTL, light/dark, large text, small screen and accessibility inspected
- [ ] API 26 and modern API 35/36 device tests, arm64-v8a install
- [ ] minSdk 26, targetSdk 36, arm64-v8a-only release APK

## Release provenance
- [ ] Release key held outside Git; APK release-signed and apksigner verified
- [ ] Commit pushed, CI passed, clean checkout and tag point to the APK source
- [ ] SHA-256, metadata, size and signature recorded
- [ ] Exact uploaded artifact downloaded, hash matched and installed
- [ ] GitHub prerelease with release notes and APK attached
