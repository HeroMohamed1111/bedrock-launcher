package com.bedrocklaunch.data.dao

import androidx.room.*
import com.bedrocklaunch.data.entity.*
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────
// Profile DAO
// ─────────────────────────────────────────────

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY last_used DESC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE is_active = 1 LIMIT 1")
    suspend fun getActiveProfile(): ProfileEntity?

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: ProfileEntity)

    @Update
    suspend fun update(profile: ProfileEntity)

    @Delete
    suspend fun delete(profile: ProfileEntity)

    @Query("UPDATE profiles SET is_active = 0")
    suspend fun clearActiveProfile()

    @Query("UPDATE profiles SET is_active = 1, last_used = :timestamp WHERE id = :id")
    suspend fun setActiveProfile(id: String, timestamp: Long = System.currentTimeMillis())
}

// ─────────────────────────────────────────────
// Favorite Servers DAO
// ─────────────────────────────────────────────

@Dao
interface FavoriteServerDao {
    @Query("SELECT * FROM favorite_servers ORDER BY added_at DESC")
    fun getAllFavorites(): Flow<List<FavoriteServerEntity>>

    @Query("SELECT * FROM favorite_servers WHERE id = :id")
    suspend fun getById(id: String): FavoriteServerEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_servers WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(server: FavoriteServerEntity)

    @Delete
    suspend fun delete(server: FavoriteServerEntity)

    @Query("DELETE FROM favorite_servers WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE favorite_servers SET ping = :ping, player_count = :playerCount WHERE id = :id")
    suspend fun updatePing(id: String, ping: Long, playerCount: Int)
}

// ─────────────────────────────────────────────
// Downloads DAO
// ─────────────────────────────────────────────

@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads ORDER BY created_at DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('PENDING','RUNNING','PAUSED') ORDER BY created_at ASC")
    fun getActiveDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id")
    suspend fun getById(id: String): DownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET downloaded_bytes = :bytes, progress = :progress, status = :status WHERE id = :id")
    suspend fun updateProgress(id: String, bytes: Long, progress: Int, status: String)

    @Query("UPDATE downloads SET status = :status, error_message = :error, completed_at = :completedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, error: String? = null, completedAt: Long? = null)

    @Delete
    suspend fun delete(download: DownloadEntity)

    @Query("DELETE FROM downloads WHERE status IN ('COMPLETED','CANCELLED','FAILED')")
    suspend fun clearFinished()
}

// ─────────────────────────────────────────────
// Cached Mods DAO
// ─────────────────────────────────────────────

@Dao
interface CachedModDao {
    @Query("SELECT * FROM cached_mods WHERE is_bookmarked = 1 ORDER BY cached_at DESC")
    fun getBookmarkedMods(): Flow<List<CachedModEntity>>

    @Query("SELECT * FROM cached_mods WHERE id = :id")
    suspend fun getById(id: String): CachedModEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mod: CachedModEntity)

    @Update
    suspend fun update(mod: CachedModEntity)

    @Query("UPDATE cached_mods SET is_bookmarked = :bookmarked WHERE id = :id")
    suspend fun setBookmarked(id: String, bookmarked: Boolean)

    @Delete
    suspend fun delete(mod: CachedModEntity)

    @Query("DELETE FROM cached_mods WHERE is_bookmarked = 0 AND cached_at < :before")
    suspend fun pruneOldCache(before: Long)
}

// ─────────────────────────────────────────────
// Installed Packs DAO
// ─────────────────────────────────────────────

@Dao
interface InstalledPackDao {
    @Query("SELECT * FROM installed_packs ORDER BY installed_at DESC")
    fun getAllPacks(): Flow<List<InstalledPackEntity>>

    @Query("SELECT * FROM installed_packs WHERE type = :type ORDER BY name ASC")
    fun getPacksByType(type: String): Flow<List<InstalledPackEntity>>

    @Query("SELECT * FROM installed_packs WHERE uuid = :uuid")
    suspend fun getByUuid(uuid: String): InstalledPackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pack: InstalledPackEntity)

    @Update
    suspend fun update(pack: InstalledPackEntity)

    @Delete
    suspend fun delete(pack: InstalledPackEntity)

    @Query("UPDATE installed_packs SET is_enabled = :enabled WHERE uuid = :uuid")
    suspend fun setEnabled(uuid: String, enabled: Boolean)
}
