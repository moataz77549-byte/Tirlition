package com.rateel.app.data.remote
import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.*
interface RadioRemoteDataSource{suspend fun fetchRadios():AppResult<List<RadioStation>>}
interface ReciterRemoteDataSource{suspend fun fetchReciters():AppResult<List<Reciter>>}
interface QuranAudioRemoteDataSource{suspend fun fetchMushafs(reciterId:String):AppResult<List<Mushaf>>;suspend fun fetchTracks(mushafId:String):AppResult<List<SurahAudio>>}
