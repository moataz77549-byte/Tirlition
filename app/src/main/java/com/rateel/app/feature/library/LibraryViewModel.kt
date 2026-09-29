package com.rateel.app.feature.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.data.local.FavoriteDao
import com.rateel.app.data.local.ListeningHistoryDao
import com.rateel.app.data.local.LocalRecordingEntity
import com.rateel.app.data.local.RecordingDao
import com.rateel.app.download.RateelDownloadManager
import com.rateel.app.domain.model.ContentCapabilities
import com.rateel.app.domain.model.PlaybackItem
import com.rateel.app.domain.model.PlaybackType
import com.rateel.app.domain.model.SourceRightsStatus
import com.rateel.app.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val dao: RecordingDao,
    private val historyDao: ListeningHistoryDao,
    private val favoriteDao: FavoriteDao,
    downloadManager: RateelDownloadManager,
    private val player: PlaybackController,
    @ApplicationContext private val context: Context,
) : ViewModel() {
    val recordings = dao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val history = historyDao.observeRecent().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val favorites = favoriteDao.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val downloads = downloadManager.downloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private fun file(item: LocalRecordingEntity): File? {
        val directory = File(context.filesDir, "recordings").canonicalFile
        return runCatching { File(item.filePath).canonicalFile }.getOrNull()
            ?.takeIf { it.parentFile == directory && it.isFile }
    }
    fun play(id: String) {
        val queue = recordings.value.mapNotNull { entry ->
            val local = file(entry) ?: return@mapNotNull null
            PlaybackItem(id = entry.id, type = PlaybackType.LOCAL_RECORDING,
                title = entry.title, subtitle = entry.stationName, artwork = entry.artwork,
                sourceId = entry.sourceId, remoteUri = null, localUri = local.toURI().toString(),
                mimeType = entry.mimeType, isLive = false, durationMs = entry.durationMs,
                stationId = entry.stationId,
                capabilities = ContentCapabilities(true, false, false, true, false, false, null,
                    SourceRightsStatus.STREAM_ONLY))
        }
        val start = queue.indexOfFirst { it.id == id }
        if (start >= 0) player.playQueue(queue, start)
    }
    fun rename(id: String, title: String) = viewModelScope.launch {
        title.trim().take(120).takeIf { it.isNotEmpty() }?.let { dao.rename(id, it) }
    }
    fun delete(id: String) = viewModelScope.launch {
        val entry = dao.get(id) ?: return@launch
        val local = file(entry)
        if (local == null || local.delete()) dao.delete(id)
    }
    fun clearHistory() = viewModelScope.launch {
        historyDao.clearAll()
    }
    fun clearFavorites() = viewModelScope.launch {
        favoriteDao.clearAll()
    }
    fun removeFavorite(type: String, id: String) = viewModelScope.launch {
        favoriteDao.delete(type, id)
    }
    fun createShareIntent(id: String): Intent? {
        val entry = recordings.value.firstOrNull { it.id == id } ?: return null
        val local = file(entry) ?: return null
        val uri = runCatching {
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                local,
            )
        }.getOrNull() ?: Uri.fromFile(local)

        return Intent(Intent.ACTION_SEND).apply {
            type = entry.mimeType.ifBlank { "audio/*" }
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, entry.title)
            putExtra(Intent.EXTRA_TEXT, entry.title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
