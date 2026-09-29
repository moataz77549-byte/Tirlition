package com.rateel.app.feature.download

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rateel.app.data.local.DownloadEntity
import com.rateel.app.data.settings.AppSettings
import com.rateel.app.download.RateelDownloadManager
import com.rateel.app.domain.model.SurahAudio
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val manager: RateelDownloadManager, private val settings: AppSettings,
) : ViewModel() {
    val downloads = manager.downloads.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val wifiOnly = settings.preferences.map { it.wifiOnlyDownloads }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val error = MutableStateFlow<String?>(null)
    val storage = MutableStateFlow(Triple(0L, 0L, 0L))
    fun refreshStorage() = viewModelScope.launch { storage.value = Triple(manager.storageBytes(), manager.recordingsBytes(), manager.freeBytes()) }
    fun download(track: SurahAudio, reciterId: String? = null) = viewModelScope.launch {
        manager.enqueue(track, reciterId).onFailure { error.value = it.message }
    }
    fun batch(tracks: List<SurahAudio>, reciterId: String? = null) = viewModelScope.launch {
        val results = manager.enqueueBatch(tracks, reciterId)
        error.value = results.firstOrNull { it.isFailure }?.exceptionOrNull()?.message
    }
    fun pause(row: DownloadEntity) = viewModelScope.launch { manager.pause(row.id) }
    fun resume(row: DownloadEntity) = viewModelScope.launch { manager.resume(row.id) }
    fun cancel(row: DownloadEntity) = viewModelScope.launch { manager.cancel(row.id) }
    fun delete(row: DownloadEntity) = viewModelScope.launch { manager.delete(row.id); refreshStorage() }
    fun deleteMushaf(id: String) = viewModelScope.launch { manager.deleteMushaf(id); refreshStorage() }
    fun wifiOnly(value: Boolean) = viewModelScope.launch { manager.wifiOnly(value) }
}
