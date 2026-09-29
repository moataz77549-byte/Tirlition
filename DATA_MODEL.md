# Stage 3 data model

`PlaybackItem` has a stable ID, type, source, remote/local URI, title, metadata, live flag and capabilities. `PlaybackProgressEntity` stores non-live ID, position, duration, completion and update time. `ListeningHistoryEntity` records the latest play per content ID, title snapshot, source and played duration. `LocalRecordingEntity` stores the local path, station/source IDs, title, timestamps, duration, size, MIME/codec, mode, rights snapshot and completion. Room migration 2→3 adds the latter fields without destructive fallback. Recordings are app-managed and never synced to Supabase.

## DownloadEntity (Room v4)
Stable contentId unique index, source/reciter/mushaf/surah identity, URL snapshot, rights snapshot, status, byte counts, validators, optional checksum, local URI, retry and verification timestamps. Migration 3→4 preserves prior rows and fills contentId from their original ID. Statuses used: QUEUED, DOWNLOADING, PAUSED, VERIFYING, COMPLETED, FAILED, CANCELLED, MISSING_FILE, EXPIRED. Wi-Fi is a WorkManager constraint; WAITING_FOR_NETWORK is reserved for UI state synchronization.
