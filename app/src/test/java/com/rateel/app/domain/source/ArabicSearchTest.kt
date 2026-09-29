package com.rateel.app.domain.source

import org.junit.Assert.assertTrue
import org.junit.Test

class ArabicSearchTest {
    @Test fun foldsHamzaAndHarakatWithoutChangingDisplayText() {
        assertTrue(ArabicSearch.matches("إبراهيم القارئ", "ابراهيم"))
        assertTrue(ArabicSearch.matches("الْقُرْآن", "القرآن"))
        assertTrue(ArabicSearch.matches("سورة البقرة", "البقره"))
        assertTrue(ArabicSearch.matches("يحيى حوا", "يحيي"))
        assertTrue(ArabicSearch.matches("الـقـرآن", "القرآن"))
    }
}
