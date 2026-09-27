package com.rateel.app.domain.repository
import com.rateel.app.core.model.AppResult
import com.rateel.app.domain.model.*
import kotlinx.coroutines.flow.Flow
interface RadioRepository{fun observeRadios():Flow<List<RadioStation>>;suspend fun refresh():AppResult<Unit>}
interface ReciterRepository{fun observeReciters():Flow<List<Reciter>>;suspend fun refresh():AppResult<Unit>}
interface MushafRepository{fun observeMushafs(reciterId:String):Flow<List<Mushaf>>}
interface AudioRepository{fun observeTracks(mushafId:String):Flow<List<SurahAudio>>}
