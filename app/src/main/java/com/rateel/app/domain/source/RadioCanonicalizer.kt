package com.rateel.app.domain.source

import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.StreamEndpoint

object RadioCanonicalizer {
    fun canonicalId(sourceId: String, providerStationId: String): String =
        sourceId.trim().lowercase() + ":" + providerStationId.trim().lowercase()

    /**
     * Combines duplicate representations of the same station while preserving all unique endpoints.
     * The canonicalKey must represent the same station/content, never merely a similar title.
     */
    fun deduplicate(items: List<RadioStation>): List<RadioStation> =
        items.groupBy { it.canonicalKey }.values.map { duplicates ->
            val primary = duplicates.first()
            primary.copy(
                streams = duplicates
                    .flatMap { it.streams }
                    .distinctBy(StreamEndpoint::url),
                isActive = duplicates.any { it.isActive },
                isVerified = duplicates.any { it.isVerified },
            )
        }
}
