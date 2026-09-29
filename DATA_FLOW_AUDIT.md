# Rateel data flow — 2026-09-29

## What the Android build actually uses

| Data | Source | Device persistence | Server write |
| --- | --- | --- | --- |
| Radio and reciter catalog | MP3Quran public v3 HTTPS API, via `Mp3QuranV3DataSource` | Room through `OfflineRadioRepository` and `OfflineReciterRepository`; 12-hour catalog TTL | None |
| Mushafs and available surahs | Same API's reciter catalog, `riwayat`, `suwar`, and `surah_list` | Room through `LocalMushafRepository` and `LocalAudioRepository` | None |
| Sources and rights | Built-in `SourceCatalog`, resolved again for recording/download actions | Room source rows | None |
| Favorites, history, playback progress | Media3 controller and local actions | Room on device | None |
| Preferences | Settings screen / download manager | Android DataStore | None |
| Download state and recording metadata | WorkManager/recording services | Room; audio files in app-specific storage | None |

There is one device Room database (`rateel.db`). Its explicit migrations are registered in
`AppModule.database`. The production dependency bindings use the MP3Quran adapter, not the
old empty milestone adapter or debug fake repositories. UI observes Room flows. Existing
cached metadata survives a failed refresh. The active API base in the catalog adapter is
`https://mp3quran.net/api/v3/`, with only the provider's documented `www` origin as a
bounded fallback. Stream URLs are returned by the API, not constructed for radio cards.

`BuildConfig.API_BASE_URL` is currently `https://api.rateel.invalid/`; Retrofit is registered
but this address is not a working backend. No current production screen uses it for the
catalog. This placeholder must not be described as an active connection.

## Supabase status

The repository has two catalog SQL migration files in `supabase/migrations`, but the
Android production path above does **not** call Supabase or synchronize Room to it.
Read-only inspection of the previously supplied project ref `ajhxfcdhuouumivmmerk` on
2026-09-29 found an active project named `Moatazalq`, no migration history, and no Rateel
catalog tables; its visible `providers`, `chats`, and `messages` tables had zero rows.
The other connected account listed projects named `Moatazahmedalz` and `Fadhkur`, not a
Rateel project. We did not apply a migration to these unrelated/uncertain targets.

Before any future shared catalog or rights configuration is enabled, explicitly identify
the intended Rateel project, review its existing schema and RLS, apply reviewed Git-backed
migrations, verify with read queries, and add an authorized read path. Secrets and a
`service_role` key must stay off Android. Stage 0 does not create a backend.

## User-visible network failure

The screenshots from the earlier RC show an empty radio list and a network error. This
is a real unresolved device observation, not proof of an API outage. The adapter now tries
the provider's documented canonical API host before its documented `www` host. CI probes
the live catalog response and counts; only a retest of the new signed APK on the affected
phone can prove whether the device's network, DNS/TLS, provider reachability, or another
factor caused the failure. There is no fabricated offline station seed.
