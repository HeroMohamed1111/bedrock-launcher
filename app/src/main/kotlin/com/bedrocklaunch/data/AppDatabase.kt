package com.bedrocklaunch.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.bedrocklaunch.data.dao.*
import com.bedrocklaunch.data.entity.*

/**
 * Room database — single source of truth for all persisted app state.
 * Version bumps require a migration or fallbackToDestructiveMigration (dev only).
 */
@Database(
    entities = [
        ProfileEntity::class,
        FavoriteServerEntity::class,
        DownloadEntity::class,
        CachedModEntity::class,
        InstalledPackEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun favoriteServerDao(): FavoriteServerDao
    abstract fun downloadDao(): DownloadDao
    abstract fun cachedModDao(): CachedModDao
    abstract fun installedPackDao(): InstalledPackDao
}
