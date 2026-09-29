# Stage 0 — UI/UX & Architecture Consolidation

Reviewed 2026-09-29 against the Compose screens on `feature/rateel-stage5` at `8da24f7`.
This is a code review, not a claim of device validation. Preserve the single Media3 service,
rights resolver, Room repositories, and download worker. The prior RC APK predates this review.

| Surface | Decision | Finding and action |
| --- | --- | --- |
| Root navigation | KEEP / IMPROVE | Five bottom destinations already exist. Settings and secondary routes remain outside the bar; add Privacy and Storage. |
| Home | IMPROVE | A large central spinner obscured cached content. Replace it with a compact loading card and progress line; keep the existing offline data flow. Continue Listening still requires a focused history integration. |
| Radios | IMPROVE | Provider ID and enum health were shown on every card. Show name, category, and useful availability; filter only on actual categories. |
| Radio detail | IMPROVE | Remove raw source ID and enum from the primary layout. Recording still follows `canRecord`. |
| Reciters and mushafs | IMPROVE | Remove source IDs from list rows, retain available surah count. A full offline state needs a repository join with canonical `availableSurahs`. |
| Surah detail | IMPROVE | Keep real play/download actions, hide technical source ID and false capability action. |
| Full player | REDESIGN | Use one vertically scrollable visual hierarchy, larger primary playback target, clear previous/next labels, and a secondary content information dialog. Required per-item attribution remains visible. Media3 is unchanged. |
| Mini player | IMPROVE | Retain the single session state and position above bottom navigation; use the theme's container color. |
| Downloads | REDESIGN | Replace a wall of track jobs with collapsible mushaf groups and useful sections. Retain pause, resume, retry, cancel, delete. Display names are currently limited by missing title snapshots in `DownloadEntity`; do not invent them. |
| Library | IMPROVE | Hide empty headings, make recordings compact, resolve radio favorite names from cached catalog. More direct navigation and mushaf names remain follow-up work. |
| Settings | IMPROVE | Keep the working theme choice; link Storage, Privacy, and About. No switches for unimplemented worship features. |
| About and rights | MERGE | About is the central entrance to sources and rights, app ownership, and privacy. Source cards expand for detailed fields; required attribution is retained. |
| Placeholder UI | REMOVE | Do not expose the existing generic `Placeholder` as a navigation destination. No worship routes ship yet. |
| Network and offline states | IMPROVE | Show cached lists during refresh; empty and error states need device and TalkBack verification. |

## Navigation and future boundaries

Root destinations: Home, Radios, Reciters, Downloads, Library. Secondary routes:
Player, Settings, Storage, Privacy, About, Sources & Rights, radio/reciter/mushaf/surah details.
No sixth tab or dead worship route is registered.

Future Worship Hub concept (documentation only): Prayer & Adhan, Adhkar, Tasbeeh,
Personal Alarms. These features should own separate domain contracts. Prayer must not
depend on radio; tasbeeh must not depend on playback; alarms must work without network.
Future audio flows use the existing playback and download infrastructure, with source
rights checked before persistent storage. Future storage categories belong in the
existing dashboard after actual data exists.

## Verification still required

- Test RTL, TalkBack, font scale, 360dp, tablet, light/dark, API 26 and API 36 on devices.
- Test mini player insets and full player scroll while the keyboard/system bars are visible.
- Review source-specific attribution against its current terms before hiding any required label.
- Observe a real large download and partial mushaf group; verify Arabic titles and byte progress.
- Stage 3/4 gaps recorded in `FINAL_AUDIT.md` remain release gates. No new APK from this branch is a Beta release until these gates pass.
