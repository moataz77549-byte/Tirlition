package com.rateel.app.domain.model
import org.junit.Assert.*
import org.junit.Test
class ModelsTest{
 @Test fun radio_supports_multiple_endpoints(){val radio=RadioStation("1","إذاعة",streams=listOf(StreamEndpoint("https://a"),StreamEndpoint("https://b")));assertEquals(2,radio.streams.size)}
 @Test fun mushaf_is_separate_from_reciter(){val mushaf=Mushaf("m1","r1","حفص","حفص عن عاصم");assertEquals("r1",mushaf.reciterId);assertEquals(114,mushaf.totalSurahs)}
}
