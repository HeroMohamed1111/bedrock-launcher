package com.bedrocklaunch.model

import java.io.Serializable

// ─────────────────────────────────────────────
// Minecraft installation info
// ─────────────────────────────────────────────

data class MinecraftInfo(
    val packageName: String = "com.mojang.minecraftpe",
    val versionName: String,
    val versionCode: Long,
    val installPath: String,
    val dataPath: String,
    val isInstalled: Boolean,
    val lastUpdated: Long
) : Serializable

// ─────────────────────────────────────────────
// Profile — stores per-user/config overrides
// ─────────────────────────────────────────────

data class Profile(
    val id: String,
    val name: String,
    val iconPath: String? = null,
    val createdAt: Long,
    val lastUsed: Long,
    val notes: String = "",
    val isActive: Boolean = false
) : Serializable

// ─────────────────────────────────────────────
// Mod / Addon models
// ─────────────────────────────────────────────

enum class ModSource { CURSEFORGE, MODRINTH, LOCAL }

data class Mod(
    val id: String,
    val name: String,
    val summary: String,
    val description: String = "",
    val author: String,
    val downloadCount: Long = 0,
    val rating: Float = 0f,
    val iconUrl: String? = null,
    val screenshots: List<String> = emptyList(),
    val latestVersion: String = "",
    val gameVersions: List<String> = emptyList(),
    val categories: List<String> = emptyList(),
    val downloadUrl: String = "",
    val fileSize: Long = 0,
    val source: ModSource,
    val projectUrl: String = "",
    val updatedAt: Long = 0
) : Serializable

// ─────────────────────────────────────────────
// Server models
// ─────────────────────────────────────────────

data class ServerInfo(
    val id: String,
    val name: String,
    val host: String,
    val port: Int = 19132,
    val motd: String = "",
    val iconUrl: String? = null,
    val gameMode: String = "",
    val version: String = "",
    val playerCount: Int = 0,
    val maxPlayers: Int = 0,
    val ping: Long = -1L, // -1 = unreachable
    val isFavorite: Boolean = false,
    val isVerified: Boolean = false,
    val tags: List<String> = emptyList()
) : Serializable

// ─────────────────────────────────────────────
// Download models
// ─────────────────────────────────────────────

enum class DownloadStatus {
    PENDING, RUNNING, PAUSED, COMPLETED, FAILED, CANCELLED
}

data class DownloadItem(
    val id: String,
    val name: String,
    val url: String,
    val destPath: String,
    val totalBytes: Long = 0L,
    val downloadedBytes: Long = 0L,
    val status: DownloadStatus = DownloadStatus.PENDING,
    val progress: Int = 0,        // 0–100
    val errorMessage: String? = null,
    val mimeType: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) : Serializable {
    val speedBps: Long get() = 0L // populated by service
}

// ─────────────────────────────────────────────
// News feed
// ─────────────────────────────────────────────

data class NewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val content: String = "",
    val imageUrl: String? = null,
    val author: String = "",
    val publishedAt: Long,
    val url: String,
    val source: String,
    val tags: List<String> = emptyList()
) : Serializable

// ─────────────────────────────────────────────
// Resource Packs / Behavior Packs
// ─────────────────────────────────────────────

enum class PackType { RESOURCE, BEHAVIOR, WORLD, SKIN }

data class Pack(
    val uuid: String,
    val name: String,
    val description: String = "",
    val version: String = "",
    val author: String = "",
    val iconPath: String? = null,
    val path: String,
    val type: PackType,
    val size: Long = 0,
    val isEnabled: Boolean = false
) : Serializable

// ─────────────────────────────────────────────
// Generic result wrapper
// ─────────────────────────────────────────────

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String = exception.message ?: "Unknown error") : Result<Nothing>()
    object Loading : Result<Nothing>()
}
