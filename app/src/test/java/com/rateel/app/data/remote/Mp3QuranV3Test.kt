package com.rateel.app.data.remote

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class Mp3QuranV3Test {
    private val json = Json { ignoreUnknownKeys = true }

    @Test fun radioAndLiveTvContracts() {
        val radios = json.decodeFromString<Mp3QuranRadiosResponse>(
            """{"radios":[{"id":1,"name":"إبراهيم الأخضر","url":"https://backup.qurango.net/radio/ibrahim_alakdar","recent_date":"2020-04-25 22:04:04"}]}"""
        )
        val live = json.decodeFromString<Mp3QuranLiveTvResponse>(
            """{"livetv":[{"id":3,"name":"قناة القرآن الكريم","url":"https://win.holol.com/live/quran/playlist.m3u8"},{"id":4,"name":"قناة السنة النبوية","url":"https://win.holol.com/live/sunnah/playlist.m3u8"}]}"""
        )
        assertEquals(1, radios.radios.single().id)
        assertEquals(2, live.livetv.size)
    }

    @Test fun partialAndMultipleMushafs() {
        val response = json.decodeFromString<Mp3QuranRecitersResponse>(
            """{"reciters":[{"id":1,"name":"قارئ","moshaf":[{"id":11,"name":"حفص","rewaya_id":1,"server":"https://server6.mp3quran.net/akdr/","surah_total":2,"surah_list":"1, 18"},{"id":12,"name":"ورش","rewaya_id":2,"server":"https://server6.mp3quran.net/akdr/","surah_total":1,"surah_list":"114"}]}]}"""
        )
        assertEquals(2, response.reciters.single().moshaf.size)
        assertEquals(setOf(1, 18), availableSurahs(response.reciters.single().moshaf[0].surahList))
        assertEquals(setOf(114), availableSurahs(response.reciters.single().moshaf[1].surahList))
        assertEquals(setOf(1, 114), availableSurahs("1,foo,0,115,114,1"))
    }

    @Test fun audioUrlIsCentralAndPadded() {
        val server = "https://server6.mp3quran.net/akdr/"
        assertEquals("https://server6.mp3quran.net/akdr/001.mp3", Mp3QuranAudioUrlResolver.resolve(server, 1))
        assertEquals("https://server6.mp3quran.net/akdr/002.mp3", Mp3QuranAudioUrlResolver.resolve(server, 2))
        assertEquals("https://server6.mp3quran.net/akdr/018.mp3", Mp3QuranAudioUrlResolver.resolve(server, 18))
        assertEquals("https://server6.mp3quran.net/akdr/114.mp3", Mp3QuranAudioUrlResolver.resolve(server, 114))
        assertNull(Mp3QuranAudioUrlResolver.resolve("https://evil.example/audio/", 1))
    }

    @Test fun optionalSurahFields() {
        val suwar = json.decodeFromString<Mp3QuranSuwarResponse>(
            """{"suwar":[{"id":1,"name":"الفاتحة","start_page":1,"end_page":1,"makkia":1},{"id":2,"name":"البقرة"}]}"""
        )
        assertEquals(1, suwar.suwar.first().startPage)
        assertNull(suwar.suwar.last().startPage)
    }
}
