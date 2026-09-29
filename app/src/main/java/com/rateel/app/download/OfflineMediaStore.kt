package com.rateel.app.download

import android.content.Context
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineMediaStore @Inject constructor(@ApplicationContext private val context: Context) {
    private val root get() = File(context.filesDir, "offline/quran").apply { mkdirs() }
    private fun safe(value: String): String {
        val prefix = value.replace(Regex("[^A-Za-z0-9_-]"), "_").take(60)
        val hash = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
            .take(6).joinToString("") { "%02x".format(it) }
        return "$prefix-$hash"
    }
    fun file(source: String, reciter: String, mushaf: String, surah: Int, format: String?): File {
        require(surah in 1..114)
        val extension = when (format?.lowercase()) { "aac", "m4a" -> "m4a"; else -> "mp3" }
        val dir = File(root, "${safe(source)}/${safe(reciter)}/${safe(mushaf)}").apply { mkdirs() }
        return File(dir, "%03d.%s".format(surah, extension))
    }
    fun part(target: File) = File(target.path + ".part")
    fun availableBytes(): Long = StatFs(root.path).availableBytes
    fun enough(bytes: Long?): Boolean = bytes == null || availableBytes() > bytes + 100L * 1024 * 1024
    fun usage(): Long = root.walkTopDown().filter { it.isFile && !it.name.endsWith(".part") }.sumOf { it.length() }
    fun recordingsUsage(): Long = File(context.filesDir, "recordings").takeIf { it.exists() }
        ?.walkTopDown()?.filter { it.isFile }?.sumOf { it.length() } ?: 0
}
