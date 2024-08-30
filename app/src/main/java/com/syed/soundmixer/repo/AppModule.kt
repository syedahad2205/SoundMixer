package com.syed.soundmixer.repo

import android.content.Context
import androidx.room.Room
import com.syed.soundmixer.room.AppDatabase
import com.syed.soundmixer.room.SavedSoundsDao
import com.syed.soundmixer.service.ApiService
import com.syed.soundmixer.service.AuthenticatedRetrofit
import com.syed.soundmixer.service.NormalRetrofit
import com.syed.soundmixer.sound.SoundDownloader
import com.syed.soundmixer.sound.SoundPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val BASE_URL = "https://freesound.org/apiv2/"

    private const val AUTH_TOKEN = "Token 4puQdpCufO35tDpaq4FOFHxqzBQ4Mh3RHINOF26l"

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideAuthenticatedRetrofit(): Retrofit {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val originalRequest = chain.request()
                val newRequest = originalRequest.newBuilder()
                    .header("Authorization", AUTH_TOKEN)
                    .build()
                chain.proceed(newRequest)
            }
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @NormalRetrofit
    fun provideNormalRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addNetworkInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(120, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedRetrofit
    fun provideApiServiceAuthenticated(@AuthenticatedRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    @NormalRetrofit
    fun provideApiServiceNormal(@NormalRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSoundDownloader(
        @ApplicationContext context: Context,
        savedSoundsDao: SavedSoundsDao,
        @NormalRetrofit apiServiceNa: ApiService
    ): SoundDownloader {
        return SoundDownloader(
            context, savedSoundsDao = savedSoundsDao,
            apiServiceNA = apiServiceNa
        )
    }

    @Provides
    @Singleton
    fun provideSoundPlayer(): SoundPlayer {
        return SoundPlayer()
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideDownloadedSoundDao(database: AppDatabase): SavedSoundsDao {
        return database.savedSoundsDao()
    }
}
