# رتيل | Rateel

Rateel is an Arabic-first Android application for Quranic and Islamic radio streams, recitations, reciters, and audio mushafs.

## Milestone 2 — Real audio library

Milestone 2 builds on the milestone-1 foundation. It connects the existing offline-first architecture to real, documented content sources without adding the full Media3 player, background playback, recording engine, or download manager yet.

### Active data flow

```
MP3Quran API v3
      ↓
Network DTOs
      ↓
Mappers / URL resolver / provenance
      ↓
Domain models + capability policy
      ↓
Room
      ↓
Repositories / StateFlow
      ↓
Compose UI
```

Room remains the metadata source observed by UI. A controlled refresh updates Room in the background. Failed refreshes do not clear the last successful metadata.

### Current sources

- **MP3Quran API v3** — primary source for languages, surah metadata, riwayat, reciters, mushafs, available surahs, radios, and live-TV metadata.
- **Qurango** — tracked independently when MP3Quran radio endpoints resolve to Qurango hosts.
- **MP3Quran Live TV** — Quran/Sunna live-channel metadata is modeled as audio-from-live-channel and treated as stream-only unless downstream rights are verified.
- **Quran Foundation** — optional secondary integration contract only. Confidential credentials are never shipped in Android; Content API access requiring credentials must go through a Rateel backend.

See [SOURCES_AND_RIGHTS.md](SOURCES_AND_RIGHTS.md).

### API endpoints used

The MP3Quran client targets `https://www.mp3quran.net/api/v3/` and supports:

- `languages`
- `suwar?language=ar`
- `riwayat?language=ar`
- `reciters?language=ar`
- `recent_reads?language=ar` (contract available; not a dependency for the primary library)
- `radios?language=ar`
- `live-tv?language=ar`

`last_updated_date` is not used as an incremental-sync authority yet. The provider describes it as filtering records added after a date, which is not sufficient by itself to model all changes/deletions safely.

### Audio URL resolution

Mushaf `server` values are mapped through one `Mp3QuranAudioUrlResolver`. Surah files use three-digit numbers such as `001.mp3`, `018.mp3`, and `114.mp3`. The pattern is covered by unit tests so a provider change is localized to this resolver.

### Offline-first metadata

Metadata refresh uses a source sync record with a six-hour TTL. On app start:

1. cached Room data is immediately observable;
2. stale metadata is refreshed;
3. successful responses replace only the relevant source data;
4. failures preserve the last successful cache and surface a non-destructive message.

### UI in milestone 2

The following screens are now backed by real repository data:

- Radios, with local search.
- Reciters, with local search.
- Reciter → available audio mushafs.
- Mushaf → available surahs only.
- Sources & Rights.

Navigation is prepared for a unified future player route. Media3 playback itself belongs to milestone 3.

### Rights and capabilities

External content carries `sourceId`; stream assets additionally preserve `returnedBySourceId`, original/resolved host metadata, and asset-rights state. UI receives `ContentCapabilities` rather than deciding rights from provider names.

Download and recording engines are intentionally not implemented in this milestone. Their future actions must use the same capability policy and rights snapshots.

### Architecture

The first release remains modular by package rather than Gradle multi-module. Boundaries exist for `core`, `data`, `domain`, `feature`, `download`, and `playback`.

### Android / build

- minSdk 26 — Android 8.0.
- targetSdk 36.
- compileSdk 36.
- JDK 17.
- Kotlin + Jetpack Compose + Material 3.
- Hilt, Room, DataStore, Retrofit/OkHttp, Kotlin Serialization, Coroutines/Flow, Coil.

Room schema is version 3 and uses explicit 1→2 and 2→3 migrations. Destructive production migration is forbidden.

With Gradle 8.13 and JDK 17:

```
gradle testDebugUnitTest lintDebug assembleDebug
```

GitHub Actions runs the same verification for `main`, feature branches, and pull requests.

### Security

Production secrets must never be committed. Cleartext traffic remains disabled globally. If a verified legacy HTTP stream is ever required, use a narrowly scoped Network Security Config instead of enabling cleartext for the whole app.

### Source of truth

This GitHub repository is the single source of truth. Official builds/releases must come from a pushed commit with a clean, traceable Git state. Milestone 2 is not a production release.
