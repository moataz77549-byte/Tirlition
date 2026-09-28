package com.rateel.app.data.remote.mp3quran

import java.util.Locale

object Mp3QuranAudioUrlResolver {
    fun resolve(server: String, surahNumber: Int): String {
        require(surahNumber in 1..114) { "surahNumber must be in 1..114" }
        val base = server.trim().trimEnd('/') + "/"
        val fileName = String.format(Locale.US, "%03d.mp3", surahNumber)
        return base + fileName
    }
}
