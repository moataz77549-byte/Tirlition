package com.rateel.app.playback

/** Finite retries; a new endpoint is selected only after repeated playback errors. */
class StreamFailoverManager {
    private var failures = 0
    private var endpoint = 0
    fun reset() { failures = 0; endpoint = 0 }
    fun onReady() { failures = 0 }
    fun next(endpointCount: Int): RetryDecision? {
        if (endpointCount <= 0) return null
        failures++
        if (failures > 3) {
            endpoint++
            failures = 1
        }
        if (endpoint >= endpointCount) return null
        return RetryDecision(endpoint, when (failures) { 1 -> 2_000L; 2 -> 5_000L; else -> 10_000L })
    }
}
data class RetryDecision(val endpointIndex: Int, val delayMs: Long)
