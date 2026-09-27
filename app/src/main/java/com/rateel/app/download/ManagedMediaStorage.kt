package com.rateel.app.download

data class StorageSummary(
    val recordingsBytes: Long,
    val downloadedMushafsBytes: Long,
    val freeBytes: Long,
)

interface ManagedMediaStorage {
    /**
     * Implementations use app-managed private storage by default.
     * Export to public Downloads requires an explicit user action in a later milestone.
     */
    suspend fun summary(): StorageSummary
    suspend fun hasSpaceFor(requiredBytes: Long): Boolean
}
