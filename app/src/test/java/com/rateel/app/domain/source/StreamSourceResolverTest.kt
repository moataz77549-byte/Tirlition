package com.rateel.app.domain.source

import com.rateel.app.domain.model.SourceIds
import org.junit.Assert.assertEquals
import org.junit.Test

class StreamSourceResolverTest {
    @Test
    fun qurango_host_gets_its_own_source_id() {
        assertEquals(
            SourceIds.QURANGO_STREAMS,
            StreamSourceResolver.sourceIdFor(
                "https://Qurango.net/radio/example",
                SourceIds.MP3_QURAN_V3,
            ),
        )
    }

    @Test
    fun unknown_host_keeps_parent_source() {
        assertEquals(
            SourceIds.MP3_QURAN_V3,
            StreamSourceResolver.sourceIdFor(
                "https://example.org/stream.mp3",
                SourceIds.MP3_QURAN_V3,
            ),
        )
    }
}
