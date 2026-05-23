package com.bedrocklaunch.di

import android.content.Context
import androidx.room.Room
import com.bedrocklaunch.api.curseforge.CurseForgeApi
import com.bedrocklaunch.api.modrinth.ModrinthApi
import com.bedrocklaunch.data.AppDatabase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // ── Gson ─────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    // ── OkHttp ────────────────────────────────────────────

    @Provides
    @Singleton
    @Named("base")
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    @Provides
    @Singleton
    @Named("curseforge")
    fun provideCurseForgeOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            // CurseForge public API key — replace with your own in production
            val req = chain.request().newBuilder()
                .addHeader("X-API-Key", "\$2a\$10\$bL4bIL5pUWqfcO7KwqK5MusmO.BuNBpY0WqF8X7pABqgC/a8Fj3t2")
                .addHeader("Accept", "application/json")
                .build()
            chain.proceed(req)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    // ── Retrofit ──────────────────────────────────────────

    @Provides
    @Singleton
    fun provideModrinthApi(gson: Gson, @Named("base") client: OkHttpClient): ModrinthApi =
        Retrofit.Builder()
            .baseUrl("https://api.modrinth.com/v2/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ModrinthApi::class.java)

    @Provides
    @Singleton
    fun provideCurseForgeApi(gson: Gson, @Named("curseforge") client: OkHttpClient): CurseForgeApi =
        Retrofit.Builder()
            .baseUrl("https://api.curseforge.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(CurseForgeApi::class.java)

    // ── Room ──────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "bedrock_launch.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideProfileDao(db: AppDatabase) = db.profileDao()
    @Provides fun provideFavoriteServerDao(db: AppDatabase) = db.favoriteServerDao()
    @Provides fun provideDownloadDao(db: AppDatabase) = db.downloadDao()
    @Provides fun provideCachedModDao(db: AppDatabase) = db.cachedModDao()
    @Provides fun provideInstalledPackDao(db: AppDatabase) = db.installedPackDao()
}
