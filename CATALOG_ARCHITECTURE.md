# Rateel catalog, stage 2

The public MP3Quran v3 adapter reads `languages`, `suwar`, `riwayat`, `reciters`, `radios`, and `live-tv`. Network DTOs are decoded defensively, mapped to Rateel domain IDs, then persisted by repositories in Room. UI observes Room flows. A failed refresh never clears existing rows.

IDs retain provider identity: `mp3quran:radio:{id}`, `mp3quran:reciter:{id}`, `mp3quran:mushaf:{id}`, and `{mushafId}:{surahNumber}`. The parser uses the source's `surah_list`; it does not invent missing tracks. `Mp3QuranAudioUrlResolver` constructs three-digit MP3 filenames only for verified MP3Quran hosts. The official endpoint was checked with HTTP HEAD for 001, 002, 018, and 114 on 2026-09-28.

Radio station identity is separate from endpoint URL. Qurango stream hosts use a distinct source ID. Other asset hosts returned by the API receive streaming-only capabilities until their rights are reviewed. Live TV channels use a separate source record and remain streaming-only. The URL is fetched from the API, not bundled.

The user-facing source and rights catalog is local today. Its booleans can later be updated by a trusted remote catalog without changing the Android model. `ContentCapabilityResolver` combines source rights and asset host, and stage 3/4 must re-evaluate capabilities before recording or downloading.

Room schema v2 adds a serialized available-surah set to mushafs and a surah metadata table. Migration 1→2 is explicit. There is no destructive fallback. Device-specific favorites, history, and download/recording records stay local.

Current limits: playback starts in stage 3; downloading starts in stage 4. Source health is `UNKNOWN` until a playback attempt or bounded on-demand HEAD check. The validator records redirect destination, host, and content type without opening all stations. API metadata refreshes at most every 12 hours on catalog entry, or immediately by explicit retry, and stays in Room for offline display.
