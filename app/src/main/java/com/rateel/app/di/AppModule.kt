package com.rateel.app.di

import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rateel.app.BuildConfig
import com.rateel.app.core.network.NetworkMonitor
import com.rateel.app.core.network.NetworkStatusProvider
import com.rateel.app.data.local.AudioTrackDao
import com.rateel.app.data.local.MushafDao
import com.rateel.app.data.local.RadioDao
import com.rateel.app.data.local.RateelDatabase
import com.rateel.app.data.local.RateelMigrations
import com.rateel.app.data.local.ReciterDao
import com.rateel.app.data.local.SourceDao
import com.rateel.app.data.local.SurahMetadataDao
import com.rateel.app.data.local.CacheMetadataDao
import com.rateel.app.data.remote.Mp3QuranV3DataSource
import com.rateel.app.data.remote.QuranAudioRemoteDataSource
import com.rateel.app.data.remote.RadioRemoteDataSource
import com.rateel.app.data.remote.ReciterRemoteDataSource
import com.rateel.app.data.repository.LocalAudioRepository
import com.rateel.app.data.repository.LocalMushafRepository
import com.rateel.app.data.repository.LocalSourceRepository
import com.rateel.app.data.repository.OfflineRadioRepository
import com.rateel.app.data.repository.OfflineReciterRepository
import com.rateel.app.domain.repository.AudioRepository
import com.rateel.app.domain.repository.MushafRepository
import com.rateel.app.domain.repository.RadioRepository
import com.rateel.app.domain.repository.ReciterRepository
import com.rateel.app.domain.repository.SourceRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): RateelDatabase =
        Room.databaseBuilder(context, RateelDatabase::class.java, "rateel.db")
            .addMigrations(*RateelMigrations.all)
            .build()

    @Provides fun sourceDao(db: RateelDatabase): SourceDao = db.sourceDao()
    @Provides fun radioDao(db: RateelDatabase): RadioDao = db.radioDao()
    @Provides fun reciterDao(db: RateelDatabase): ReciterDao = db.reciterDao()
    @Provides fun mushafDao(db: RateelDatabase): MushafDao = db.mushafDao()
    @Provides fun audioTrackDao(db: RateelDatabase): AudioTrackDao = db.audioTrackDao()
    @Provides fun surahMetadataDao(db: RateelDatabase): SurahMetadataDao = db.surahMetadataDao()
    @Provides fun cacheMetadataDao(db: RateelDatabase): CacheMetadataDao = db.cacheMetadataDao()

    @Provides
    @Singleton
    fun okHttp(@ApplicationContext context: Context): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .cache(Cache(File(context.cacheDir, "http_metadata"), 20L * 1024L * 1024L))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "Rateel-Android/" + BuildConfig.VERSION_NAME)
                        .build(),
                )
            }

        if (BuildConfig.DEBUG) {
            builder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                },
            )
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(
                Json { ignoreUnknownKeys = true }
                    .asConverterFactory("application/json".toMediaType()),
            )
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingsModule {
    @Binds abstract fun bindNetworkStatus(impl: NetworkMonitor): NetworkStatusProvider
    @Binds abstract fun bindRadioRemote(impl: Mp3QuranV3DataSource): RadioRemoteDataSource
    @Binds abstract fun bindReciterRemote(impl: Mp3QuranV3DataSource): ReciterRemoteDataSource
    @Binds abstract fun bindQuranAudioRemote(impl: Mp3QuranV3DataSource): QuranAudioRemoteDataSource
    @Binds abstract fun bindSourceRepository(impl: LocalSourceRepository): SourceRepository
    @Binds abstract fun bindRadioRepository(impl: OfflineRadioRepository): RadioRepository
    @Binds abstract fun bindReciterRepository(impl: OfflineReciterRepository): ReciterRepository
    @Binds abstract fun bindMushafRepository(impl: LocalMushafRepository): MushafRepository
    @Binds abstract fun bindAudioRepository(impl: LocalAudioRepository): AudioRepository
}
