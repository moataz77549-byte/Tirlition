package com.rateel.app.domain.repository

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.ContentSource
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.SurahAudio
import kotlinx.coroutines.flow.Flow

interface SourceRepository {
    fun observeSources(): Flow<List<ContentSource>>
    suspend fun getSource(id: String): ContentSource?
    suspend fun ensureBuiltInCatalog()
}

interface RadioRepository {
    fun observeRadios(): Flow<List<RadioStation>>
    suspend fun refresh(): AppResult<Unit>
}

interface ReciterRepository {
    fun observeReciters(): Flow<List<Reciter>>
    suspend fun refresh(): AppResult<Unit>
}

interface MushafRepository {
    fun observeMushafs(reciterId: String): Flow<List<Mushaf>>
    suspend fun refresh(reciterId: String): AppResult<Unit>
}

interface AudioRepository {
    fun observeTracks(mushafId: String): Flow<List<SurahAudio>>
    suspend fun refresh(mushafId: String): AppResult<Unit>
}
