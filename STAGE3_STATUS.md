# Stage 3 verification status

Stage 3 was merged into `main` by PR #3 (`9b40676`). Its implementation is present, but the acceptance criteria still require physical/emulator playback and an authorized-source recording test. It must not be described as fully accepted before those tests pass. No Release or production tag was created.

| Area | Current implementation | Remaining verification |
| --- | --- | --- |
| Engine/session | Single Media3 ExoPlayer owned by MediaSessionService, MediaController abstraction, audio focus and becoming noisy | Notification, lock screen, Bluetooth/headset and call interruption tests on devices |
| UI | Mini player, full player, playback controls, radio/reciter/surah routes, Arabic/English strings | RTL, dark mode, rotation and accessibility inspection on devices |
| Queue | Current radio list and available mushaf surahs; previous/next | Restore multi-item queue after process death |
| Resume | Room progress on non-live items, stable-ID paused item restoration, no automatic audio | Process-death tests, playback completion edge cases |
| Stream resilience | Bounded 2/5/10-second retry; refresh by station ID; switch to next endpoint | Wi-Fi/mobile transitions, actual backup endpoint and HLS live tests |
| Sleep | Service timer 5/10/15/30/45/60 minutes and end of surah | Process-death persistence and device test |
| Recording | Rights gate in recorder, separate progressive MP3/AAC network stream, free/5/10/15/30 minutes, .part file, header/size validation, local Room metadata | Real authorized source, full decoder validation, network/disk/app-kill tests |
| Foreground recording | dataSync foreground service, ongoing notification and Stop action | Notification permission/Android 14-16 device checks |
| Library | Local recordings list/play/rename/delete, favorites and recent history | Offline file playback test on device; sharing only if rights allow |
| Rights | Production `canRecord=false` remains in effect; HLS recording unsupported; no third-party rights inferred | Obtain explicit source-level permission before production recording acceptance |
| Database | Explicit Room 2→3 migration; no destructive fallback | Upgrade install test with a real version-2 database |
| CI | Unit tests, compile instrumented tests, lint, debug build, release Kotlin compilation | Instrumented tests execution and manual acceptance tests |

Current live-TV API links were observed returning HTTP 404 during stage 2 verification on 2026-09-28. They remain catalog metadata rather than a claimed working live stream. Surah MP3 URLs and a Qurango MP3 stream were probed successfully, but Media3 playback on a device has not been verified here.
