package com.rateel.app.di
import android.content.Context
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rateel.app.BuildConfig
import com.rateel.app.data.local.*
import com.rateel.app.data.settings.AppSettings
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class) object AppModule{
@Provides @Singleton fun db(@ApplicationContext c:Context)=Room.databaseBuilder(c,RateelDatabase::class.java,"rateel.db").build()
@Provides fun radioDao(db:RateelDatabase)=db.radioDao()
@Provides fun reciterDao(db:RateelDatabase)=db.reciterDao()
@Provides @Singleton fun settings(@ApplicationContext c:Context)=AppSettings(c)
@Provides @Singleton fun okHttp():OkHttpClient=OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(30,TimeUnit.SECONDS).addInterceptor{chain->chain.proceed(chain.request().newBuilder().header("User-Agent","Rateel-Android/"+BuildConfig.VERSION_NAME).build())}.apply{if(BuildConfig.DEBUG)addInterceptor(HttpLoggingInterceptor().apply{level=HttpLoggingInterceptor.Level.BASIC})}.build()
@Provides @Singleton fun retrofit(client:OkHttpClient):Retrofit=Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL).client(client).addConverterFactory(Json{ignoreUnknownKeys=true}.asConverterFactory("application/json".toMediaType())).build()
}
