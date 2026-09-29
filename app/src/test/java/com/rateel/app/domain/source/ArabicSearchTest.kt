package com.rateel.app.domain.source

import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicSearchTest {
    @Test fun foldsHamzaAndHarakatWithoutChangingDisplayText() {
        assertTrue(ArabicSearch.matches("إبراهيم القارئ", "ابراهيم"))
        assertTrue(ArabicSearch.matches("الْقُرْآن", "القرآن"))
    }
}
