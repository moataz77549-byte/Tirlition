package com.rateel.app.download

data class StorageSummary(
    val recordingsBytes: Long,
    val downloadedMushafsBytes: Long,
    val freeBytes: Long,
)

data class DownloadPlan(
    val sourceId: String,
    val contentIds: List<String>,
    val estimatedBytes: Long?,
    val wifiOnly: Boolean,
)

enum class DownloadCommand { PAUSE, RESUME, CANCEL, RETRY }

interface ManagedMediaStorage {
    /** App-managed private storage is the default; public export requires explicit user action. */
    suspend fun summary(): StorageSummary
    suspend fun hasSpaceFor(requiredBytes: Long): Boolean
}

interface DownloadVerifier {
    /** A completed file must exist and be non-empty; trusted checksums are verified when available. */
    suspend fun verify(localUri: String, expectedChecksum: String?): Boolean
}

interface DownloadController {
    suspend fun enqueue(plan: DownloadPlan): Result<List<String>>
    suspend fun command(downloadId: String, command: DownloadCommand): Result<Unit>
}
