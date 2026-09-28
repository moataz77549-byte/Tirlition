package com.rateel.app.data.remote.mp3quran

import com.rateel.app.domain.model.AssetRightsStatus
import com.rateel.app.domain.model.SourceIds
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test

class Mp3QuranContractTest {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Test
    fun reciters_parse_multiple_and_partial_mushafs() {
        val fixture = """{"reciters":[{"id":231,"name":"قارئ","letter":"ق","future_field":1,"moshaf":[{"id":231,"name":"حفص","server":"https://server11.mp3quran.net/hazza/","surah_total":3,"moshaf_type":11,"surah_list":"1,18,114"},{"id":280,"name":"قالون","server":"https://server16.mp3quran.net/deban/","surah_total":2,"moshaf_type":51,"surah_list":"2, 3"}]}]}"""
        val response = json.decodeFromString<Mp3QuranRecitersResponse>(fixture)
        val suwar = Mp3QuranSuwarResponse(
            listOf(
                Mp3QuranSurahDto(1, "الفاتحة"),
                Mp3QuranSurahDto(2, "البقرة"),
                Mp3QuranSurahDto(3, "آل عمران"),
                Mp3QuranSurahDto(18, "الكهف"),
                Mp3QuranSurahDto(114, "الناس"),
            ),
        )
        val catalog = Mp3QuranMapper.mapCatalog(response, suwar)
        assertEquals(1, catalog.reciters.size)
        assertEquals(2, catalog.mushafs.size)
        assertEquals(setOf(1, 18, 114), catalog.mushafs.first().availableSurahs)
        assertEquals(5, catalog.tracks.size)
        val track = catalog.tracks.first { it.surahNumber == 18 }
        assertEquals("https://server11.mp3quran.net/hazza/018.mp3", track.audioUrl)
        assertEquals("mp3quran:reciter:231", track.reciterId)
    }

    @Test
    fun surah_list_is_defensive_and_domain_ready() {
        assertEquals(listOf(1, 2, 114), Mp3QuranMapper.parseSurahList("1,2,2,bad,0,115,114,"))
    }

    @Test
    fun radios_json_preserves_mp3quran_and_qurango_provenance() {
        val fixture = """{"radios":[{"id":10,"name":"إذاعة","url":"https://Qurango.net/radio/example","future_field":"ignored"}]}"""
        val response = json.decodeFromString<Mp3QuranRadiosResponse>(fixture)
        val mapped = Mp3QuranMapper.mapRadios(response).single()
        assertEquals(SourceIds.MP3_QURAN_V3, mapped.sourceId)
        assertEquals(SourceIds.QURANGO_STREAMS, mapped.streams.single().sourceId)
        assertEquals(SourceIds.MP3_QURAN_V3, mapped.streams.single().returnedBySourceId)
        assertEquals(AssetRightsStatus.STREAM_ONLY, mapped.streams.single().assetRightsStatus)
    }

    @Test
    fun live_tv_json_is_hls_and_stream_only() {
        val fixture = """{"livetv":[{"id":3,"name":"قناة القرآن","url":"https://win.holol.com/live/quran/playlist.m3u8"},{"id":4,"name":"قناة السنة","url":"https://win.holol.com/live/sunnah/playlist.m3u8"}]}"""
        val response = json.decodeFromString<Mp3QuranLiveTvResponse>(fixture)
        val mapped = Mp3QuranMapper.mapLiveTv(response).first()
        assertEquals("hls", mapped.streams.single().format)
        assertEquals(AssetRightsStatus.STREAM_ONLY, mapped.streams.single().assetRightsStatus)
        assertTrue(mapped.tags.contains("audio-from-live-channel"))
        assertEquals(2, Mp3QuranMapper.mapLiveTv(response).size)
    }

    @Test
    fun invalid_stream_urls_are_rejected_without_crashing_catalog() {
        val response = Mp3QuranRadiosResponse(
            listOf(
                Mp3QuranRadioDto(1, "تالف", "not-a-url"),
                Mp3QuranRadioDto(2, "آمن", "https://qurango.net/radio/good"),
            ),
        )
        val mapped = Mp3QuranMapper.mapRadios(response)
        assertEquals(1, mapped.size)
        assertEquals("mp3quran:radio:2", mapped.single().id)
    }

    @Test
    fun http_stream_is_retained_but_blocked_until_narrow_cleartext_policy_exists() {
        val mapped = Mp3QuranMapper.mapRadios(
            Mp3QuranRadiosResponse(listOf(Mp3QuranRadioDto(7, "قديم", "http://qurango.net/radio/legacy"))),
        ).single()
        assertEquals(com.rateel.app.domain.model.StreamHealth.BLOCKED, mapped.streams.single().health)
    }
}
