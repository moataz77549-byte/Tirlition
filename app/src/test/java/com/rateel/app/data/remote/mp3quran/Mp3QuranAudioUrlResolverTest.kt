package com.rateel.app.data.remote.mp3quran

import org.junit.Assert.assertEquals
import org.junit.Test

class Mp3QuranAudioUrlResolverTest {
    @Test fun pads_surah_numbers_exactly_once() {
        val base = "https://server6.mp3quran.net/akdr/"
        assertEquals(base + "001.mp3", Mp3QuranAudioUrlResolver.resolve(base, 1))
        assertEquals(base + "002.mp3", Mp3QuranAudioUrlResolver.resolve(base, 2))
        assertEquals(base + "018.mp3", Mp3QuranAudioUrlResolver.resolve(base, 18))
        assertEquals(base + "114.mp3", Mp3QuranAudioUrlResolver.resolve(base, 114))
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejects_non_absolute_media_server() {
        Mp3QuranAudioUrlResolver.resolve("server/path", 1)
    }

    @Test(expected = IllegalArgumentException::class)
    fun rejects_out_of_quran_range() {
        Mp3QuranAudioUrlResolver.resolve("https://server.example/", 115)
    }
}
