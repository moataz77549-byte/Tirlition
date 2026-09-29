# Rateel architecture

The catalog uses MP3Quran v3 DTO → mapper → domain models → repositories → Room and ViewModels. SourceRegistry and ContentCapabilityResolver determine rights for each asset. See CATALOG_ARCHITECTURE.md and SOURCES_AND_RIGHTS.md.

Playback uses a single Media3 ExoPlayer inside UnifiedPlaybackService, a MediaSession for Android system controls, MediaController for the UI abstraction, Room for progress/history, and a separate rights-gated StreamRecorder boundary. See PLAYBACK_ARCHITECTURE.md and RECORDING_ARCHITECTURE.md for the current implementation and outstanding work.
