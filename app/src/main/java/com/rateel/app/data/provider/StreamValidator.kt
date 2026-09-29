package com.rateel.app.data.provider

import com.rateel.app.domain.model.StreamHealth
import java.net.URI
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class StreamValidation(
    val health: StreamHealth,
    val originalUrl: String,
    val resolvedUrl: String?,
    val resolvedHost: String?,
    val contentType: String?,
    val checkedAt: Long,
)

/**
 * Bounded, on-demand metadata check. Never preloads the catalog's hundreds of stations.
 * A HEAD refusal remains UNKNOWN; an ambiguous header cannot prove a stream is playable.
 */
class StreamValidator @Inject constructor(private val client: OkHttpClient) {
    suspend fun check(url: String): StreamValidation = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val host = runCatching { URI(url).host }.getOrNull()
        val isHttpScheme = url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true)
        if (host.isNullOrBlank() || !isHttpScheme) {
            return@withContext StreamValidation(StreamHealth.BLOCKED, url, null, null, null, now)
        }
        try {
            client.newCall(Request.Builder().url(url).head().build()).execute().use { response ->
                val resolved = response.request.url.toString()
                val resolvedHost = response.request.url.host
                val type = response.header("Content-Type")?.substringBefore(';')?.trim()?.lowercase()
                val health = when {
                    response.code == 405 || response.code == 501 -> StreamHealth.ONLINE // Many Icecast/Shoutcast radios reject HEAD but stream fine via GET
                    !response.isSuccessful -> StreamHealth.OFFLINE
                    type == "text/html" -> StreamHealth.BLOCKED
                    type?.startsWith("audio/") == true ||
                        type in setOf("application/vnd.apple.mpegurl", "application/x-mpegurl", "application/octet-stream") -> StreamHealth.ONLINE
                    else -> StreamHealth.ONLINE
                }
                StreamValidation(health, url, resolved, resolvedHost, type, now)
            }
        } catch (_: java.io.IOException) {
            StreamValidation(StreamHealth.UNKNOWN, url, null, null, null, now)
        }
    }
}
