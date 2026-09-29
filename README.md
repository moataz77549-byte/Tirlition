# رتيل | Rateel

Rateel is an Arabic-first Android application for Quranic and Islamic radio streams, recitations, reciters, and audio mushafs.

## Milestone 1 — Foundation

This milestone establishes the Android foundation only. It intentionally does not implement advanced playback, background media controls, production downloads, live recording, favorites/history UX, or the later Quran/text/prayer modules.

## Milestone 2 — Audio catalog (in progress)

MP3Quran v3 now provides Arabic radio, reciter, mushaf, riwaya, surah, and live-channel metadata through a dedicated HTTPS adapter. The repository stores the normalized catalog in Room and displays cached radio/reciter/mushaf/surah lists offline. The Quran and Sunna live channels are audio-first catalog entries. Their HLS assets are streaming-only while broadcaster rights remain unverified.

The next milestone provides Media3 playback. Catalog detail pages do not pretend to play audio. Downloads and stream recording remain unavailable until their dedicated milestones and source capability checks are enforced.

### Stack

Kotlin, Jetpack Compose + Material 3, Navigation Compose, Coroutines/Flow, Room, DataStore, Retrofit/OkHttp, Kotlin Serialization, Hilt, and Coil.

### Architecture

The first version is modular by package rather than Gradle multi-module. This keeps build complexity low while preserving boundaries for core, data, domain, feature, download, and playback. The playback package is a boundary for a later Media3 service and must remain independent from individual screens/ViewModels.

Data is designed offline-first: Room is the persistent source observed by UI-facing repositories; remote data sources refresh Room; HTTP caching is separate from persistent/media caching.

### Android versions

- minSdk 26 — Android 8.0, broad compatibility without unnecessary legacy complexity.
- targetSdk 36.
- compileSdk 36.
- Java/JDK 17.

### Database

Room starts at schema version 1. Production code must use explicit migrations as versions increase. Destructive production migration is forbidden. The schema reserves normalized tables for content sources/rights, radio streams, reciters, mushafs, audio tracks, favorites, listening history, downloads metadata, playback progress, cache metadata, and future recordings.

### Sources and rights

Rights are business logic, not a credits-only page. External content carries `sourceId`, and `SourceRightsPolicy` gates streaming, download, offline playback, recording, sharing, and commercial-use decisions. Remote enable/disable switches allow a source or capability to be disabled without publishing a new APK.

The technical/legal registry is [SOURCES_AND_RIGHTS.md](SOURCES_AND_RIGHTS.md). Planned providers are denied by default until their permissions are verified. The settings navigation contains a Sources & Rights screen backed by the source repository.

### Networking and security

Production secrets must never be committed. `API_BASE_URL` is currently a non-routable placeholder until a real Rateel backend/source is selected. Cleartext traffic is disabled globally. If a legacy HTTP station is later required, add a narrowly scoped Network Security Config instead of enabling cleartext globally. Debug-only HTTP logging is enabled at BASIC level.

Quran Foundation integrations that require a `client_secret` must be proxied through a Rateel backend; the secret must never be embedded in the APK.

### Build

The repository includes GitHub Actions verification. Locally, with Gradle 8.13 and JDK 17:

    gradle testDebugUnitTest lintDebug assembleDebug

### Source of truth

This GitHub repository is the single source of truth. Official builds/releases must come from a pushed commit, with a clean and traceable Git state. This milestone is not a production release.

## Playback (stage 3 development)

One Media3 ExoPlayer in MediaSessionService plays MP3/AAC radios, HLS when the endpoint is valid, remote surahs and local recordings. MediaSession provides Android media controls in the foreground/background; the UI has a mini and full player. Room stores non-live progress and recent listening. The app does not automatically start audio on launch. Radio reconnect/failover uses finite retries and resolves endpoints from the repository. The implementation still needs device validation and process restoration.

The recorder boundary accepts only progressive MP3/AAC from a verified source and host with explicit recording rights; HLS recording is unsupported. Current catalog sources do not grant recording, so the production UI intentionally omits recording. See PLAYBACK_ARCHITECTURE.md and RECORDING_ARCHITECTURE.md.

## Offline audio development
The stage-4 branch adds rights-gated, app-private audio downloads, a Wi-Fi-only preference, and local-first playback in the existing Media3 engine. Downloads remain subject to device and CI verification. No final APK or release is produced at this stage.
