package com.rateel.app.data.remote

import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.Mushaf
import com.rateel.app.domain.model.RadioStation
import com.rateel.app.domain.model.Reciter
import com.rateel.app.domain.model.SurahAudio
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Safe production defaults for milestone 1.
 * Real providers are introduced in later milestones without changing repository/UI contracts.
 */
@Singleton
class EmptyRadioRemoteDataSource @Inject constructor() : RadioRemoteDataSource {
    override suspend fun fetchRadios(): AppResult<List<RadioStation>> = AppResult.Empty
}

@Singleton
class EmptyReciterRemoteDataSource @Inject constructor() : ReciterRemoteDataSource {
    override suspend fun fetchReciters(): AppResult<List<Reciter>> = AppResult.Empty
}

@Singleton
class EmptyQuranAudioRemoteDataSource @Inject constructor() : QuranAudioRemoteDataSource {
    override suspend fun fetchMushafs(reciterId: String): AppResult<List<Mushaf>> = AppResult.Empty
    override suspend fun fetchTracks(mushafId: String): AppResult<List<SurahAudio>> = AppResult.Empty
}
