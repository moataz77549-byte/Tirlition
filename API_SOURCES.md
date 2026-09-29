# API source adapters and verification

The Android production bindings point to `Mp3QuranV3DataSource`, not the empty milestone-1 or debug fake repositories. It reads MP3Quran v3 `radios?language=ar`, `reciters?language=ar`, `suwar?language=ar`, `riwayat?language=ar`, `live-tv` and `languages` over HTTPS. The JSON decoder ignores unknown keys and reports network, timeout, parsing and server failures. Mushaf tracks come only from the provider's `surah_list`, using its `server` and a three-digit surah filename. This URL pattern is restricted to MP3Quran-owned HTTPS hosts in `Mp3QuranAudioUrlResolver`.

Qurango endpoints appear as radio assets when returned by MP3Quran, but retain a separate `sourceId`. Live-TV channel URLs are provider-returned metadata; the broadcaster's host and rights do not inherit MP3Quran's permissions. The 2026-09-28 probe in STAGE3_STATUS.md observed HTTP 404 for live-TV assets, so they require a new end-to-end availability test before a working-channel claim.

For downloads, headers `Content-Length`, `ETag`, `Last-Modified`, `Content-Range` and 206 are handled per transfer. No blanket guarantee is made that every host or file supports Range; a 200 response restarts from zero. Redirects are checked against the MP3Quran host for MP3Quran downloads. Check low, middle and 114 tracks across multiple reciters before publishing. Current live API and ten-station playback matrix is pending, as documented in FINAL_AUDIT.md.

Provider references: https://www.mp3quran.net/ar/api/2 and https://www.mp3quran.net/privacy-en.html.
