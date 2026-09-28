create table public.surah_metadata (
  number integer primary key check (number between 1 and 114),
  name text not null,
  start_page integer check (start_page is null or start_page between 1 and 604),
  end_page integer check (end_page is null or end_page between 1 and 604),
  is_makki boolean,
  source_id text not null references public.content_sources(id),
  updated_at timestamptz not null default now()
);
alter table public.surah_metadata enable row level security;
revoke all on public.surah_metadata from anon, authenticated;
grant select on public.surah_metadata to anon, authenticated;
grant select, insert, update, delete on public.surah_metadata to service_role;
create policy "Read surah metadata" on public.surah_metadata for select to anon, authenticated using (true);
insert into public.content_sources (
 id, name, provider, type, website, api_base_url, documentation_url, license_type,
 allow_streaming, is_official, is_verified, last_technical_check_at, notes
) values (
 'mp3quran-live-tv', 'MP3Quran live channels', 'MP3Quran.net / channel broadcaster',
 'CONTENT_PROVIDER', 'https://www.mp3quran.net/',
 'https://www.mp3quran.net/api/v3/live-tv',
 'https://www.mp3quran.net/ar/api/2', 'UNKNOWN',
 true, true, true, 1790553600000,
 'Streaming only; downstream HLS broadcaster recording and download rights unverified.'
);
