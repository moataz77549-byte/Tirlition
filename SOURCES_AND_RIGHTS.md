# Rateel sources and rights registry

This file is the technical/legal registry for external content used by Rateel. A public API or stream is not, by itself, proof of every downstream right. Unknown capabilities are denied by default.

Last review: **2026-09-28**.

## mp3quran-v3

- **Source ID:** `mp3quran-v3`
- **Provider:** MP3Quran.net
- **Priority:** primary v1 metadata/audio source.
- **Content types:** languages, surah metadata, riwayat, reciters, mushafs, available surahs, Quran audio URLs, radio catalog, and live metadata.
- **API base:** https://www.mp3quran.net/api/v3/
- **Official API documentation:** https://www.mp3quran.net/eng/api
- **Rights statement:** https://www.mp3quran.net/privacy-en.html
- **Additional official statement:** https://www.mp3quran.net/eng/contact-us
- **Reviewed rights statement:** MP3Quran states that its materials/links are available for visitors and developers to copy/use. Rateel still keeps downstream-host provenance separate rather than assuming that every third-party-hosted asset inherits every possible right.
- **Streaming:** enabled for verified MP3Quran material.
- **Download / offline playback:** enabled for verified MP3Quran Quran-audio material under the reviewed provider statement; the asset policy can still reduce this capability.
- **Caching / offline sync:** enabled for Rateel metadata/audio use covered by the reviewed provider statement.
- **Recording:** not explicitly enabled by Rateel policy; disabled.
- **Sharing downloaded media files:** disabled unless separately verified. Sharing a source URL is not treated as unrestricted file redistribution.
- **Commercial use:** not explicitly verified for Rateel; disabled.
- **Attribution:** no mandatory attribution requirement was identified in the reviewed MP3Quran statement, but Rateel preserves and displays source identity.
- **Rights checked:** 2026-09-28.
- **Technical check:** 2026-09-28.
- **Implementation:** active in milestone 2 through Retrofit DTOs, mappers, Room, repositories, and Compose UI. Results are not frozen as an APK-embedded station/reciter list.

## qurango-streams

- **Source ID:** `qurango-streams`
- **Provider:** MP3Quran.net / Qurango.net
- **Content types:** live radio transport endpoints returned by MP3Quran.
- **Website:** https://qurango.net/
- **Relevant MP3Quran policy:** https://www.mp3quran.net/privacy-en.html
- **Rights status:** MP3Quran's published policy states that the same policy applies to Qurango.net.
- **Streaming:** enabled.
- **Recording:** disabled pending explicit verification for live-stream recording.
- **Download / offline playback:** disabled for ordinary live endpoints.
- **Sharing files / commercial use:** disabled pending explicit verification.
- **Rights checked:** 2026-09-28.
- **Technical handling:** station catalog provenance remains `mp3quran-v3`; an endpoint whose verified host is `qurango.net` or a subdomain receives `qurango-streams` as its stream source. Rateel does not classify look-alike domains as Qurango.

## mp3quran-live-tv

- **Source ID:** `mp3quran-live-tv`
- **Provider / returned by:** MP3Quran API v3 `live-tv`.
- **Content types:** live Quran/Sunna channel metadata. These may be HLS/m3u8 and are modeled as `AUDIO_FROM_LIVE_CHANNEL` for the audio-first v1 experience.
- **Official API documentation:** https://www.mp3quran.net/eng/api
- **Rights statement:** https://www.mp3quran.net/privacy-en.html
- **Streaming:** enabled when the asset is technically compatible.
- **Download:** disabled.
- **Recording:** disabled.
- **Offline playback / sharing:** disabled.
- **Asset-host rule:** the endpoint returned by MP3Quran records its original URL, host, redirect result, and asset-rights status. A third-party final host is not automatically granted broader rights by being returned from the API.
- **Rights checked:** 2026-09-28.
- **Implementation:** catalog source and endpoint provenance are kept separate. The app does not hardcode the live channel URL.

## quran-foundation

- **Source ID:** `quran-foundation`
- **Provider:** Quran Foundation
- **Role:** secondary/optional; disabled until a Rateel backend and exact content scope are configured.
- **API:** https://apis.quran.foundation/
- **Documentation:** https://api-docs.quran.foundation/
- **Developer terms:** https://api-docs.quran.foundation/legal/developer-terms/
- **Content Sync:** https://api-docs.quran.foundation/docs/tutorials/content-sync/getting-started/
- **Credential rule:** confidential Content API credentials are server-side only. No `client_secret` belongs in BuildConfig, resources, native code, APK, or Git.
- **Generic caching:** limited by the current provider terms; Rateel models a seven-day generic retention limit unless an explicit exception or supported Content Sync policy applies.
- **Offline sync:** only through the documented scope/policy; periodic re-sync is modeled.
- **User-export/download:** disabled until the exact resource/use is verified.
- **Recording / raw redistribution:** disabled.
- **Attribution:** required conservatively and resource-specific requirements must also be honored.
- **ID safety:** Chapter Reciter IDs and Ayah-by-Ayah Recitation IDs are represented as different Android types to prevent accidental mixing.
- **Rights checked:** 2026-09-28.
- **Operational status:** disabled / `REQUIRES_BACKEND`.

## Other official broadcasters

Saudi Quran radio, Makkah Quran broadcasts, Madinah/Sunnah broadcasts, and other official stations require their own Source IDs and rights records before production enablement. Public stream availability does not prove permission to record, download, redistribute, or share.

## Enforcement rules

1. Every external `RadioStation`, `StreamEndpoint`, `Reciter`, `Mushaf`, and `SurahAudio` carries a non-empty `sourceId`.
2. Stream assets additionally track `returnedBySourceId`, `assetHost`, original/resolved URLs, redirect host, and `assetRightsStatus`.
3. Runtime actions use `SourceRightsPolicy` + `ContentCapabilityResolver`; UI does not infer permissions from provider names.
4. Download metadata preserves source, remote/local URI, content identity, checksum, download date, and a rights snapshot.
5. A stored rights snapshot documents the policy at creation time; it never overrides a newer restrictive policy.
6. Fallbacks are eligible only for the same canonical media identity and must independently pass the fallback source's current rights policy.
7. Runtime records include switches for disabling a source or individual capability without changing UI logic or rebuilding the feature architecture.
8. App-private managed storage is the default for future downloads and recordings.
9. Recording means saving permitted stream bytes, never microphone capture.
10. Provider-rights changes require updating this registry and runtime policy before enabling broader behavior.
