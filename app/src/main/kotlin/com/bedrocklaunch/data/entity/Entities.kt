package com.bedrocklaunch.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.bedrocklaunch.model.DownloadStatus
import com.bedrocklaunch.model.ModSource
import com.bedrocklaunch.model.PackType

// ─────────────────────────────────────────────
// Profile entity
// ─────────────────────────────────────────────

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "icon_path") val iconPath: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "last_used") val lastUsed: Long = System.currentTimeMillis(),
    val notes: String = "",
    @ColumnInfo(name = "is_active") val isActive: Boolean = false
)

// ─────────────────────────────────────────────
// Favorite server entity
// ─────────────────────────────────────────────

@Entity(tableName = "favorite_servers")
data class FavoriteServerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val host: String,
    val port: Int = 19132,
    val motd: String = "",
    @ColumnInfo(name = "icon_url") val iconUrl: String? = null,
    @ColumnInfo(name = "game_mode") val gameMode: String = "",
    val version: String = "",
    @ColumnInfo(name = "player_count") val playerCount: Int = 0,
    @ColumnInfo(name = "max_players") val maxPlayers: Int = 0,
    val ping: Long = -1L,
    @ColumnInfo(name = "is_verified") val isVerified: Boolean = false,
    val tags: String = "",    // JSON array stored as string
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Download record entity
// ─────────────────────────────────────────────

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val id: String,
    val name: String,
    val url: String,
    @ColumnInfo(name = "dest_path") val destPath: String,
    @ColumnInfo(name = "total_bytes") val totalBytes: Long = 0L,
    @ColumnInfo(name = "downloaded_bytes") val downloadedBytes: Long = 0L,
    val status: String = DownloadStatus.PENDING.name,
    val progress: Int = 0,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
    @ColumnInfo(name = "mime_type") val mimeType: String = "",
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null
)

// ─────────────────────────────────────────────
// Cached mod entity (for offline/bookmarked)
// ─────────────────────────────────────────────

@Entity(tableName = "cached_mods")
data class CachedModEntity(
    @PrimaryKey val id: String,
    val name: String,
    val summary: String,
    val author: String,
    @ColumnInfo(name = "download_count") val downloadCount: Long = 0,
    val rating: Float = 0f,
    @ColumnInfo(name = "icon_url") val iconUrl: String? = null,
    @ColumnInfo(name = "latest_version") val latestVersion: String = "",
    @ColumnInfo(name = "game_versions") val gameVersions: String = "",  // JSON
    val categories: String = "",     // JSON
    @ColumnInfo(name = "download_url") val downloadUrl: String = "",
    @ColumnInfo(name = "file_size") val fileSize: Long = 0,
    val source: String = ModSource.MODRINTH.name,
    @ColumnInfo(name = "project_url") val projectUrl: String = "",
    @ColumnInfo(name = "updated_at") val updatedAt: Long = 0,
    @ColumnInfo(name = "is_bookmarked") val isBookmarked: Boolean = false,
    @ColumnInfo(name = "cached_at") val cachedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────
// Installed Pack entity
// ─────────────────────────────────────────────

@Entity(tableName = "installed_packs")
data class InstalledPackEntity(
    @PrimaryKey val uuid: String,
    val name: String,
    val description: String = "",
    val version: String = "",
    val author: String = "",
    @ColumnInfo(name = "icon_path") val iconPath: String? = null,
    val path: String,
    val type: String = PackType.RESOURCE.name,
    val size: Long = 0,
    @ColumnInfo(name = "is_enabled") val isEnabled: Boolean = false,
    @ColumnInfo(name = "installed_at") val installedAt: Long = System.currentTimeMillis()
)
