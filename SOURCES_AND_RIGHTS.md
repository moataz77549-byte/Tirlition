# Rateel sources and rights registry

This file is the technical/legal registry for external content used by Rateel. A source is not a production source merely because an API or stream is public. Unknown capabilities are denied by default.

Last review: **2026-09-28**.

## mp3quran-v3

- **Source ID:** `mp3quran-v3`
- **Provider:** MP3Quran.net
- **Content types:** radios, reciters, mushafs/riwayat, available surahs, Quran audio URLs, and supported live metadata.
- **API:** https://www.mp3quran.net/api/v3/
- **Official documentation:** https://www.mp3quran.net/ar/api/2
- **Rights statement:** https://www.mp3quran.net/privacy-en.html
- **Additional official statement:** https://www.mp3quran.net/eng/contact-us
- **Streaming:** allowed in Rateel policy.
- **Download / offline playback:** allowed for provider material under the reviewed statement permitting copying site material; downstream items must still preserve their own provenance.
- **Caching / offline sync:** allowed.
- **Recording:** not explicitly verified; disabled.
- **Sharing downloaded media files:** not explicitly verified; disabled. Sharing provider URLs is not treated as unrestricted media redistribution.
- **Commercial use:** not explicitly verified; disabled.
- **Required attribution:** no explicit mandatory attribution found in the reviewed statement; Rateel still preserves source identity.
- **Rights checked:** 2026-09-28.
- **Technical check:** 2026-09-28.
- **Implementation:** planned primary v1 provider; actual API integration starts in milestone 2, not hard-coded UI data.

## qurango-streams

- **Source ID:** `qurango-streams`
- **Provider:** MP3Quran.net / Qurango.net
- **Content types:** live radio endpoints returned by MP3Quran.
- **Website:** https://qurango.net/
- **Relevant official policy:** https://www.mp3quran.net/privacy-en.html
- **Rights status:** MP3Quran's published policy states it also applies to Qurango.net.
- **Streaming:** allowed.
- **Recording:** not explicitly verified; disabled.
- **Download / offline playback:** disabled for ordinary live endpoints.
- **Sharing files / commercial use:** not explicitly verified; disabled.
- **Rights checked:** 2026-09-28.
- **Implementation:** station catalog provenance and stream transport provenance are separate; a Qurango endpoint receives its own `sourceId`.

## mp3quran-live-tv

- **Source ID:** `mp3quran-live-tv`
- **Returned by:** MP3Quran v3 `/live-tv`.
- **Content:** Quran and Sunna live channels. The current API points to HLS assets hosted by a separate broadcaster domain.
- **Policy:** streaming only. Recording, downloads, caching, offline playback, and sharing the media are disabled until the actual broadcaster's rights are established.
- **Technical rule:** the API response URL is fetched each refresh; no HLS URL is bundled in the APK. The resolved host is distinct from the API's source of discovery.
- **Checked:** 2026-09-28. This check confirms API metadata, not a blanket grant for media redistribution.

## quran-foundation

- **Source ID:** `quran-foundation`
- **Provider:** Quran Foundation
- **API:** https://apis.quran.foundation/
- **Documentation:** https://api-docs.quran.com/
- **Developer terms:** https://api-docs.quran.com/legal/developer-terms/
- **Content Sync:** https://api-docs.quran.com/docs/tutorials/content-sync/getting-started/
- **Streaming / in-app use:** permitted subject to provider terms and the exact production content scope.
- **Generic caching:** no longer than one week unless an explicit exception applies.
- **Offline sync:** supported for documented Content Sync resource groups; periodic sync is required.
- **User-export/download:** disabled until the exact resource/use is confirmed.
- **Recording:** disabled.
- **Raw content redistribution/sharing:** disabled.
- **Commercial application:** provider terms allow monetized applications under conditions, while selling/sublicensing/redistributing QF content or raw API data requires separate licensing.
- **Attribution:** Rateel conservatively requires provider attribution and will also honor resource-specific requirements.
- **Credential rule:** never ship a Content API `client_secret` in the APK; confidential access goes through a Rateel backend.
- **Rights checked:** 2026-09-28.
- **Operational status:** built-in record stays disabled until backend proxy/configuration and the exact content scope are approved.

## Official broadcasters

Saudi Quran radio, Makkah Quran broadcasts, Madinah/Sunnah broadcasts, and other official stations require their own Source IDs and rights records before production enablement. Public stream availability does not prove permission to record, download, redistribute, or share.

## Enforcement rules

1. Every external `RadioStation`, `StreamEndpoint`, `Reciter`, `Mushaf`, and `SurahAudio` carries a non-empty `sourceId`.
2. Runtime actions consult `SourceRightsPolicy`; hiding a button is not the only enforcement layer.
3. Download metadata preserves `sourceId`, remote URL, local URI, content identity, checksum, download date, and a rights snapshot.
4. Fallbacks may represent only the same canonical media identity and must pass the rights policy of the fallback source.
5. Runtime policy can disable a source or individual capability without a new APK.
6. App-private managed storage is the default for downloads and recordings.
7. Recording means saving stream bytes, never microphone capture.
8. Provider-rights changes require updating this registry and runtime policy before enabling new behavior.
9. `ContentCapabilityResolver` intersects source permissions with the actual asset host and live-channel type. A third-party asset never inherits MP3Quran's download/offline grant merely because the API returned its URL.

## Stage-4 download gate
The current MP3Quran v3 catalog grants direct progressive audio download and local listening only when the asset host is MP3Quran-owned. Attribution remains MP3Quran.net. Qurango radio, MP3Quran live TV and Quran Foundation audio cannot enter the download queue. Recording and sharing are separate capabilities and remain disabled for those assets. An API link or reachable URL is never itself permission. See the provider's published policy and verify rights changes before enabling new sources.
