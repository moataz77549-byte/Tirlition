package com.rateel.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelsTest {
    @Test
    fun radio_supports_multiple_endpoints() {
        val sourceId = SourceIds.MP3_QURAN_V3
        val radio = RadioStation(
            id = "1",
            sourceId = sourceId,
            canonicalKey = "mp3quran:1",
            nameArabic = "إذاعة",
            streams = listOf(
                StreamEndpoint(sourceId = sourceId, url = "https://a"),
                StreamEndpoint(sourceId = sourceId, url = "https://b"),
            ),
        )
        assertEquals(2, radio.streams.size)
    }

    @Test
    fun mushaf_is_separate_from_reciter() {
        val mushaf = Mushaf(
            id = "m1",
            sourceId = SourceIds.MP3_QURAN_V3,
            reciterId = "r1",
            name = "حفص",
            riwaya = "حفص عن عاصم",
        )
        assertEquals("r1", mushaf.reciterId)
        assertEquals(114, mushaf.totalSurahs)
    }
}
