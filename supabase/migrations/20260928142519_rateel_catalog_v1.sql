-- Rateel shared content catalog. Room remains the device database.
-- Public clients may read catalog rows; only trusted server/database roles may write.
-- Identifiers remain text to match the Android entities and provider IDs.

create table public.content_sources (
  id text primary key,
  name text not null,
  provider text not null,
  type text not null,
  website text,
  api_base_url text,
  documentation_url text,
  terms_url text,
  copyright_url text,
  attribution_text text,
  license_type text not null default 'UNKNOWN',
  requires_attribution boolean not null default false,
  allow_streaming boolean not null default false,
  allow_download boolean not null default false,
  allow_offline_playback boolean not null default false,
  allow_caching boolean not null default false,
  allow_offline_sync boolean not null default false,
  allow_recording boolean not null default false,
  allow_sharing boolean not null default false,
  allow_commercial_use boolean not null default false,
  max_offline_retention_days integer check (max_offline_retention_days is null or max_offline_retention_days >= 0),
  requires_periodic_sync boolean not null default false,
  is_official boolean not null default false,
  is_verified boolean not null default false,
  last_rights_check_at bigint,
  last_technical_check_at bigint,
  notes text,
  is_enabled boolean not null default true,
  streaming_enabled boolean not null default true,
  download_enabled boolean not null default true,
  offline_playback_enabled boolean not null default true,
  caching_enabled boolean not null default true,
  offline_sync_enabled boolean not null default true,
  recording_enabled boolean not null default true,
  sharing_enabled boolean not null default true,
  disabled_reason text
);

create table public.radio_stations (
  id text primary key,
  source_id text not null references public.content_sources(id),
  canonical_key text not null,
  name_arabic text not null,
  name_english text,
  description text,
  logo_url text,
  country text,
  language text not null,
  category text,
  website text,
  is_active boolean not null default true,
  is_featured boolean not null default false,
  is_verified boolean not null default false,
  health text not null default 'UNKNOWN',
  created_at bigint,
  updated_at bigint,
  unique (source_id, canonical_key)
);
create index radio_stations_source_id_idx on public.radio_stations(source_id);

create table public.radio_streams (
  id text primary key,
  radio_id text not null references public.radio_stations(id) on delete cascade,
  source_id text not null references public.content_sources(id),
  provider_endpoint_id text,
  url text not null,
  format text,
  bitrate_kbps integer check (bitrate_kbps is null or bitrate_kbps > 0),
  is_primary boolean not null default false
);
create index radio_streams_radio_id_idx on public.radio_streams(radio_id);
create index radio_streams_source_id_idx on public.radio_streams(source_id);

create table public.reciters (
  id text primary key,
  source_id text not null references public.content_sources(id),
  name_arabic text not null,
  name_english text,
  photo_url text,
  country text,
  biography text,
  featured boolean not null default false
);
create index reciters_source_id_idx on public.reciters(source_id);

create table public.mushafs (
  id text primary key,
  source_id text not null references public.content_sources(id),
  reciter_id text not null references public.reciters(id) on delete cascade,
  name text not null,
  riwaya text not null,
  description text,
  source text,
  quality text,
  format text,
  total_surahs integer not null default 0 check (total_surahs between 0 and 114),
  artwork_url text
);
create index mushafs_source_id_idx on public.mushafs(source_id);
create index mushafs_reciter_id_idx on public.mushafs(reciter_id);

create table public.audio_tracks (
  id text primary key,
  source_id text not null references public.content_sources(id),
  mushaf_id text not null references public.mushafs(id) on delete cascade,
  surah_number integer not null check (surah_number between 1 and 114),
  surah_name_arabic text not null,
  surah_name_english text,
  audio_url text not null,
  duration_ms bigint check (duration_ms is null or duration_ms >= 0),
  file_size_bytes bigint check (file_size_bytes is null or file_size_bytes >= 0),
  format text,
  bitrate_kbps integer check (bitrate_kbps is null or bitrate_kbps > 0),
  quality text,
  checksum text,
  downloadable boolean not null default false,
  unique (mushaf_id, surah_number)
);
create index audio_tracks_source_id_idx on public.audio_tracks(source_id);

-- Explicit grants are needed on new Supabase projects. No client writes.
revoke all on public.content_sources, public.radio_stations, public.radio_streams,
  public.reciters, public.mushafs, public.audio_tracks from anon, authenticated;
grant select on public.content_sources, public.radio_stations, public.radio_streams,
  public.reciters, public.mushafs, public.audio_tracks to anon, authenticated;
grant select, insert, update, delete on public.content_sources, public.radio_stations,
  public.radio_streams, public.reciters, public.mushafs, public.audio_tracks to service_role;

alter table public.content_sources enable row level security;
alter table public.radio_stations enable row level security;
alter table public.radio_streams enable row level security;
alter table public.reciters enable row level security;
alter table public.mushafs enable row level security;
alter table public.audio_tracks enable row level security;

create policy "Read source rights" on public.content_sources for select to anon, authenticated using (true);
create policy "Read verified active stations" on public.radio_stations for select to anon, authenticated
  using (is_active and is_verified and exists (
    select 1 from public.content_sources s where s.id = source_id
      and s.is_enabled and s.is_verified and s.streaming_enabled and s.allow_streaming
  ));
create policy "Read permitted streams" on public.radio_streams for select to anon, authenticated
  using (exists (
    select 1 from public.radio_stations r where r.id = radio_id
  ) and exists (
    select 1 from public.content_sources s where s.id = source_id
      and s.is_enabled and s.is_verified and s.streaming_enabled and s.allow_streaming
  ));
create policy "Read permitted reciters" on public.reciters for select to anon, authenticated
  using (exists (select 1 from public.content_sources s where s.id = source_id and s.is_enabled and s.is_verified));
create policy "Read permitted mushafs" on public.mushafs for select to anon, authenticated
  using (exists (select 1 from public.reciters r where r.id = reciter_id)
    and exists (select 1 from public.content_sources s where s.id = source_id and s.is_enabled and s.is_verified));
create policy "Read permitted tracks" on public.audio_tracks for select to anon, authenticated
  using (exists (select 1 from public.mushafs m where m.id = mushaf_id)
    and exists (select 1 from public.content_sources s where s.id = source_id
      and s.is_enabled and s.is_verified and s.streaming_enabled and s.allow_streaming));

-- Built-in rights records from PlannedSourceCatalog. They are catalog metadata,
-- not imported radio/reciter data. Unknown permissions remain disabled.
insert into public.content_sources (
  id, name, provider, type, website, api_base_url, documentation_url, terms_url,
  copyright_url, attribution_text, license_type, requires_attribution,
  allow_streaming, allow_download, allow_offline_playback, allow_caching,
  allow_offline_sync, allow_commercial_use, is_official, is_verified,
  last_rights_check_at, last_technical_check_at, notes
) values (
  'mp3quran-v3', 'MP3Quran API v3', 'MP3Quran.net', 'API',
  'https://www.mp3quran.net/', 'https://www.mp3quran.net/api/v3/',
  'https://www.mp3quran.net/ar/api/2', 'https://www.mp3quran.net/privacy-en.html',
  'https://www.mp3quran.net/eng/contact-us', 'MP3Quran.net', 'PROVIDER_TERMS', false,
  true, true, true, true, true, false, true, true,
  1790553600000, 1790553600000,
  'Recording, downloaded-file redistribution and commercial use are disabled until explicitly verified.'
), (
  'qurango-streams', 'Qurango Streams', 'MP3Quran.net / Qurango.net', 'CONTENT_PROVIDER',
  'https://qurango.net/', null, null, 'https://www.mp3quran.net/privacy-en.html',
  'https://www.mp3quran.net/eng/contact-us', 'Qurango.net', 'PROVIDER_TERMS', false,
  true, false, false, false, false, false, true, true,
  1790553600000, 1790553600000,
  'Public reachability does not establish recording permission.'
);
insert into public.content_sources (
  id, name, provider, type, website, api_base_url, documentation_url, terms_url,
  copyright_url, attribution_text, license_type, requires_attribution,
  allow_streaming, allow_offline_playback, allow_caching, allow_offline_sync,
  allow_commercial_use, max_offline_retention_days, requires_periodic_sync,
  is_official, is_verified, last_rights_check_at, last_technical_check_at,
  is_enabled, streaming_enabled, download_enabled, offline_playback_enabled,
  caching_enabled, offline_sync_enabled, recording_enabled, sharing_enabled,
  disabled_reason, notes
) values (
  'quran-foundation', 'Quran Foundation APIs', 'Quran Foundation', 'API',
  'https://quran.foundation/', 'https://apis.quran.foundation/',
  'https://api-docs.quran.com/', 'https://api-docs.quran.com/legal/developer-terms/',
  'https://api-docs.quran.com/legal/developer-terms/', 'Quran Foundation',
  'PROVIDER_TERMS', true, true, true, true, true, true, 7, true,
  true, true, 1790553600000, 1790553600000,
  false, false, false, false, false, false, false, false,
  'backend_proxy_and_content_scope_required',
  'Content APIs require server-held credentials; generic caching is limited to one week unless an exception applies.'
);
