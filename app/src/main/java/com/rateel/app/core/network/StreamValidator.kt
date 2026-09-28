package com.rateel.app.core.network

import com.rateel.app.domain.model.StreamHealth
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class StreamValidationResult(
    val health: StreamHealth,
    val originalUrl: String,
    val resolvedUrl: String? = null,
    val resolvedHost: String? = null,
    val contentType: String? = null,
    val checkedAt: Long = System.currentTimeMillis(),
    val reason: String? = null,
)

interface StreamValidator {
    suspend fun validate(url: String, allowedHosts: Set<String>? = null): StreamValidationResult
}

@Singleton
class OkHttpStreamValidator @Inject constructor(
    private val client: OkHttpClient,
) : StreamValidator {
    override suspend fun validate(url: String, allowedHosts: Set<String>?): StreamValidationResult =
        withContext(Dispatchers.IO) {
            val originalHost = host(url)
            if (allowedHosts != null && originalHost != null && !allowedHosts.any { hostMatches(originalHost, it) }) {
                return@withContext StreamValidationResult(StreamHealth.BLOCKED, url, reason = "host_not_allowed")
            }

            val headResponse = runCatching {
                client.newCall(Request.Builder().url(url).head().build()).execute()
            }.getOrElse {
                return@withContext StreamValidationResult(StreamHealth.OFFLINE, url, reason = it.javaClass.simpleName)
            }

            val response = if (headResponse.code == 405 || headResponse.code == 403) {
                headResponse.close()
                runCatching {
                    client.newCall(
                        Request.Builder()
                            .url(url)
                            .header("Range", "bytes=0-1")
                            .get()
                            .build(),
                    ).execute()
                }.getOrElse {
                    return@withContext StreamValidationResult(StreamHealth.OFFLINE, url, reason = it.javaClass.simpleName)
                }
            } else {
                headResponse
            }

            response.use {
                val resolved = it.request.url.toString()
                val resolvedHost = it.request.url.host
                if (allowedHosts != null && !allowedHosts.any { allowed -> hostMatches(resolvedHost, allowed) }) {
                    return@withContext StreamValidationResult(StreamHealth.BLOCKED, url, resolved, resolvedHost, reason = "redirect_host_not_allowed")
                }
                val type = it.header("Content-Type")?.substringBefore(';')?.trim()?.lowercase()
                val mediaLike = type == null ||
                    type.startsWith("audio/") ||
                    type in setOf("application/vnd.apple.mpegurl", "application/x-mpegurl", "application/octet-stream")
                val health = when {
                    !it.isSuccessful -> StreamHealth.OFFLINE
                    type?.startsWith("text/html") == true -> StreamHealth.UNSUPPORTED
                    mediaLike -> StreamHealth.ONLINE
                    else -> StreamHealth.DEGRADED
                }
                StreamValidationResult(health, url, resolved, resolvedHost, type)
            }
        }

    private fun host(url: String): String? = runCatching { URI(url).host?.lowercase() }.getOrNull()
    private fun hostMatches(host: String, allowed: String): Boolean =
        host == allowed || host.endsWith(".$allowed")
}
