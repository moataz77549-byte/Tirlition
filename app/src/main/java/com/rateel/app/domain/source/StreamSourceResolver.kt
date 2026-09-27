package com.rateel.app.domain.source

import com.rateel.app.domain.model.SourceIds
import java.net.URI

/**
 * Tags stream transport separately from the catalog source that described the station.
 * This lets one radio keep MP3Quran provenance while a Qurango endpoint has its own rights record.
 */
object StreamSourceResolver {
    fun sourceIdFor(url: String, parentSourceId: String): String {
        val host = runCatching { URI(url).host.orEmpty().lowercase() }.getOrDefault("")
        return if (host == "qurango.net" || host.endsWith(".qurango.net")) {
            SourceIds.QURANGO_STREAMS
        } else {
            parentSourceId
        }
    }
}
