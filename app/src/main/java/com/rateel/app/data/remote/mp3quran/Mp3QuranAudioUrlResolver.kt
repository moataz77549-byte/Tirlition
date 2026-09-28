package com.rateel.app.data.remote.mp3quran

import java.net.URI
import java.util.Locale

object Mp3QuranAudioUrlResolver {
    fun resolve(server: String, surahNumber: Int): String {
        require(surahNumber in 1..114) { "surahNumber must be in 1..114" }
        val base = server.trim().trimEnd('/') + "/"
        val uri = URI(base)
        require(uri.host != null && (uri.scheme.equals("https", true) || uri.scheme.equals("http", true))) {
            "server must be an absolute HTTP(S) URL"
        }
        val fileName = String.format(Locale.US, "%03d.mp3", surahNumber)
        return base + fileName
    }
}
