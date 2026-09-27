package com.rateel.app.domain.source

import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.RightsAction
import com.rateel.app.domain.model.SourceRightsPolicy

data class AudioSourceCandidate(
    val canonicalMediaId: String,
    val sourceId: String,
    val remoteUrl: String,
    val priority: Int,
)

/**
 * Fallbacks are eligible only for the exact same canonical media identity and only when the
 * fallback source permits the requested operation.
 */
object FallbackPolicy {
    fun allowedCandidates(
        canonicalMediaId: String,
        candidates: List<AudioSourceCandidate>,
        sources: Map<String, ContentSource>,
        action: RightsAction = RightsAction.STREAM,
    ): List<AudioSourceCandidate> =
        candidates
            .asSequence()
            .filter { it.canonicalMediaId == canonicalMediaId }
            .filter { candidate ->
                sources[candidate.sourceId]
                    ?.let { SourceRightsPolicy.evaluate(it, action).allowed }
                    ?: false
            }
            .sortedBy { it.priority }
            .toList()
}
