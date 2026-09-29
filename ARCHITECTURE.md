# Rateel architecture

The catalog uses MP3Quran v3 DTO → mapper → domain models → repositories → Room and ViewModels. SourceRegistry and ContentCapabilityResolver determine rights for each asset. See CATALOG_ARCHITECTURE.md and SOURCES_AND_RIGHTS.md.

Playback uses a single Media3 ExoPlayer inside UnifiedPlaybackService, a MediaSession for Android system controls, MediaController for the UI abstraction, Room for progress/history, and a separate rights-gated StreamRecorder boundary. See PLAYBACK_ARCHITECTURE.md and RECORDING_ARCHITECTURE.md for the current implementation and outstanding work.

## Offline audio (stage 4 in progress)
Catalog tracks and source rights → download manager → Room queue + constrained WorkManager foreground worker → private media store → local-first Media3 item. See `DOWNLOAD_ARCHITECTURE.md` and `OFFLINE_STORAGE.md` for current behavior and verification gaps.
