package com.rateel.app.domain.source

import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.SourceIds
import com.rateel.app.domain.model.StreamEndpoint
import org.junit.Assert.assertEquals
import org.junit.Test

class RadioCanonicalizerTest {
    @Test
    fun same_canonical_station_merges_unique_endpoints() {
        val source = SourceIds.MP3_QURAN_V3
        val first = RadioStation(
            id = "1",
            sourceId = source,
            canonicalKey = "radio:1",
            nameArabic = "إذاعة",
            streams = listOf(StreamEndpoint(sourceId = source, url = "https://a")),
        )
        val second = first.copy(
            id = "1-alt",
            streams = listOf(StreamEndpoint(sourceId = SourceIds.QURANGO_STREAMS, url = "https://b")),
        )

        val result = RadioCanonicalizer.deduplicate(listOf(first, second))

        assertEquals(1, result.size)
        assertEquals(2, result.single().streams.size)
    }
}
