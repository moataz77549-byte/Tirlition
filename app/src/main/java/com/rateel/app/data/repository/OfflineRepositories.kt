package com.rateel.app.data.repository
import com.rateel.app.core.model.AppResult
import com.rateel.app.data.local.*
import com.rateel.app.data.remote.*
import com.rateel.app.domain.model.*
import com.rateel.app.domain.repository.*
import kotlinx.coroutines.flow.map
class OfflineRadioRepository(private val dao:RadioDao,private val remote:RadioRemoteDataSource):RadioRepository{
 override fun observeRadios()=dao.observeAll().map{list->list.map{RadioStation(it.id,it.nameArabic,it.nameEnglish,it.description,emptyList(),it.logoUrl,it.country,it.language,it.category,it.website,it.isActive,it.isFeatured,it.isVerified,runCatching{StreamHealth.valueOf(it.health)}.getOrDefault(StreamHealth.UNKNOWN),createdAt=it.createdAt,updatedAt=it.updatedAt)}}
 override suspend fun refresh():AppResult<Unit> = when(val r=remote.fetchRadios()){is AppResult.Success->{dao.upsertAll(r.data.map{RadioEntity(it.id,it.nameArabic,it.nameEnglish,it.description,it.logoUrl,it.country,it.language,it.category,it.website,it.isActive,it.isFeatured,it.isVerified,it.health.name,it.createdAt,it.updatedAt)});AppResult.Success(Unit)};AppResult.Empty->AppResult.Empty;is AppResult.Error->r}
}
class OfflineReciterRepository(private val dao:ReciterDao,private val remote:ReciterRemoteDataSource):ReciterRepository{
 override fun observeReciters()=dao.observeAll().map{it.map{x->Reciter(x.id,x.nameArabic,x.nameEnglish,x.photoUrl,x.country,x.biography,x.featured)}}
 override suspend fun refresh():AppResult<Unit> = when(val r=remote.fetchReciters()){is AppResult.Success->{dao.upsertAll(r.data.map{ReciterEntity(it.id,it.nameArabic,it.nameEnglish,it.photoUrl,it.country,it.biography,it.featured)});AppResult.Success(Unit)};AppResult.Empty->AppResult.Empty;is AppResult.Error->r}
}
