# Rateel sources and rights registry

This file is the technical and legal registry for external content used by Rateel.

A provider is **not a production source** merely because an API or public stream is reachable. Before production enablement, its source record must document provenance, rights, attribution, and allowed capabilities. Unknown rights are treated as denied.

## Production gate

Every external RadioStation, StreamEndpoint, Reciter, Mushaf, and SurahAudio must carry a non-empty `sourceId`. A source must exist in the local/remote source registry before dependent content is accepted.

Runtime behavior is controlled by both rights and remote switches:

- streaming requires verified source rights + `allowStreaming` + `streamingEnabled`
- downloading requires verified source rights + `allowDownload` + `downloadEnabled`
- offline playback additionally requires `allowOfflinePlayback`
- recording requires `allowRecording` + `recordingEnabled`
- sharing requires `allowSharing` + `sharingEnabled`
- disabling a source remotely must block all dependent actions without a new APK

Downloaded files and local recordings retain `sourceId` plus a rights snapshot/attribution record.

## mp3quran-v3

- **Source ID:** `mp3quran-v3`
- **Provider:** MP3Quran.net
- **Status:** planned primary v1 catalog source; production rights not yet approved
- **Content types:** radios, reciters, mushafs/riwayat, surah audio metadata, live-TV metadata
- **API:** https://www.mp3quran.net/api/v3/
- **Official documentation:** https://www.mp3quran.net/ar/api/2
- **Terms / copyright:** no sufficiently explicit production-use terms have been recorded in this repository yet
- **Streaming permission:** not enabled until rights review is completed
- **Download permission:** not enabled until rights review is completed
- **Recording permission:** not enabled until rights review is completed
- **Offline playback permission:** not enabled until rights review is completed
- **Required attribution:** unknown; do not guess
- **Date technically verified:** 2026-09-28
- **Date rights verified:** not yet verified
- **Implementation notes:** API v3 documents reciters/mushafs and radio endpoints. Provider data must be normalized through adapters; no UI calls the API directly.

## qurango-streams

- **Source ID:** `qurango-streams`
- **Provider:** Qurango.net
- **Status:** planned stream-transport source; production rights not yet approved
- **Content types:** radio stream endpoints referenced by MP3Quran v3
- **API / documentation:** no independent Rateel integration is enabled
- **Terms / copyright:** not yet verified
- **Streaming permission:** not enabled until rights review is completed
- **Download permission:** not enabled
- **Recording permission:** not enabled
- **Offline playback permission:** not enabled
- **Required attribution:** unknown
- **Date technically verified:** 2026-09-28 through current MP3Quran v3 radio responses
- **Date rights verified:** not yet verified
- **Implementation notes:** a station can retain MP3Quran catalog provenance while its StreamEndpoint is tagged `qurango-streams`. Canonical station IDs prevent duplicate stations when multiple endpoints represent the same station.

## quran-foundation

- **Source ID:** `quran-foundation`
- **Provider:** Quran Foundation
- **Status:** rights terms reviewed; Android integration remains disabled until production access/content-specific review is complete
- **Content types:** Quran content APIs, including recitation metadata/content where granted
- **API documentation:** https://api-docs.quran.foundation/
- **Developer terms:** https://api-docs.quran.foundation/legal/developer-terms/
- **Terms last updated by provider:** 2026-09-14
- **Date verified by Rateel:** 2026-09-28
- **Attribution:** `Quran data provided by Quran Foundation.`
- **Caching/offline rule:** standard QF content may not be cached/stored longer than one week unless QF expressly permits longer storage or the content is covered by the documented Content Sync exception; Content Sync must be refreshed at least every 7 days.
- **Mobile secrets:** client credentials/client_secret must never ship in the APK. Secret-bearing authentication belongs on a Rateel backend.
- **Streaming/in-app use:** provider terms permit in-app use/display subject to the terms and content-specific licenses; operational streaming stays disabled until Rateel production access is approved.
- **Download / offline files:** user-visible downloadable audio remains disabled until the relevant content-specific rights are confirmed.
- **Recording:** not enabled.
- **Sharing:** raw content/file sharing is not enabled. Provider terms distinguish certain attributed social-media uses from redistribution; Rateel must not generalize that into unrestricted file sharing.
- **Commercial use:** the terms permit monetized applications under conditions, but selling/sublicensing/redistributing QF content or raw API data requires separate written licensing.
- **Implementation notes:** use an independent adapter behind the Rateel backend/gateway. Never place a QF client_secret in Android code or GitHub.

## Official broadcasters

Each broadcaster must receive its own Source ID and rights entry before production. A publicly reachable stream only establishes technical availability. It does **not** establish recording, downloading, redistribution, or sharing rights.

Until verified, such sources are streaming-disabled in production rather than guessed to be permissive.

## Change procedure

Before enabling a new source:

1. Add/update this registry entry.
2. Verify official documentation, terms, copyright/license, and attribution.
3. Record `lastRightsCheckAt` and technical verification date.
4. Set only capabilities that are actually supported by evidence.
5. Add/update the runtime `ContentSource` record and remote switches.
6. Add tests for any special policy.
7. Run tests, lint, build, commit, push, and verify the remote commit.

Never use “available on the internet” as rights evidence.
