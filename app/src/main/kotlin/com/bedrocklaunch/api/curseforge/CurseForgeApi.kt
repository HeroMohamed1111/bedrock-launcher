package com.bedrocklaunch.api.curseforge

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

// ─────────────────────────────────────────────
// CurseForge REST API v1 — Minecraft Bedrock (game 432 / category 4475)
// Base URL: https://api.curseforge.com/v1/
// Requires X-API-Key header (injected via OkHttp interceptor)
// ─────────────────────────────────────────────

interface CurseForgeApi {

    @GET("mods/search")
    suspend fun searchMods(
        @Query("gameId") gameId: Int = 432,
        @Query("searchFilter") query: String = "",
        @Query("categoryId") categoryId: Int? = null,
        @Query("gameVersion") gameVersion: String? = null,
        @Query("pageSize") pageSize: Int = 20,
        @Query("index") index: Int = 0,
        @Query("sortField") sortField: Int = 2,   // 2=popularity
        @Query("sortOrder") sortOrder: String = "desc"
    ): CurseForgeSearchResponse

    @GET("mods/{modId}")
    suspend fun getMod(@Path("modId") modId: Int): CurseForgeModResponse

    @GET("mods/{modId}/files")
    suspend fun getModFiles(
        @Path("modId") modId: Int,
        @Query("gameVersion") gameVersion: String? = null,
        @Query("pageSize") pageSize: Int = 10
    ): CurseForgeFilesResponse

    @GET("mods/{modId}/files/{fileId}/download-url")
    suspend fun getDownloadUrl(
        @Path("modId") modId: Int,
        @Path("fileId") fileId: Int
    ): CurseForgeUrlResponse
}

// ─────────────────────────────────────────────
// Response models
// ─────────────────────────────────────────────

data class CurseForgeSearchResponse(
    val data: List<CurseForgeMod>,
    val pagination: CurseForgePagination
)

data class CurseForgeModResponse(val data: CurseForgeMod)
data class CurseForgeFilesResponse(val data: List<CurseForgeFile>, val pagination: CurseForgePagination)
data class CurseForgeUrlResponse(val data: String)

data class CurseForgePagination(
    val index: Int,
    @SerializedName("pageSize") val pageSize: Int,
    @SerializedName("resultCount") val resultCount: Int,
    @SerializedName("totalCount") val totalCount: Int
)

data class CurseForgeMod(
    val id: Int,
    val gameId: Int,
    val name: String,
    val slug: String,
    val links: CurseForgeLinks,
    val summary: String,
    val status: Int,
    @SerializedName("downloadCount") val downloadCount: Long,
    @SerializedName("isFeatured") val isFeatured: Boolean,
    @SerializedName("primaryCategoryId") val primaryCategoryId: Int,
    val categories: List<CurseForgeCategory>,
    val classId: Int?,
    val authors: List<CurseForgeAuthor>,
    val logo: CurseForgeImage?,
    val screenshots: List<CurseForgeImage> = emptyList(),
    @SerializedName("mainFileId") val mainFileId: Int?,
    @SerializedName("latestFiles") val latestFiles: List<CurseForgeFile> = emptyList(),
    @SerializedName("dateCreated") val dateCreated: String = "",
    @SerializedName("dateModified") val dateModified: String = "",
    @SerializedName("dateReleased") val dateReleased: String = "",
    val allowModDistribution: Boolean? = true
)

data class CurseForgeLinks(
    @SerializedName("websiteUrl") val websiteUrl: String = "",
    @SerializedName("wikiUrl") val wikiUrl: String? = null,
    @SerializedName("issuesUrl") val issuesUrl: String? = null,
    @SerializedName("sourceUrl") val sourceUrl: String? = null
)

data class CurseForgeCategory(
    val id: Int,
    val gameId: Int,
    val name: String,
    val slug: String,
    @SerializedName("iconUrl") val iconUrl: String = ""
)

data class CurseForgeAuthor(
    val id: Int,
    val name: String,
    val url: String = ""
)

data class CurseForgeImage(
    val id: Int,
    val modId: Int,
    val title: String,
    val description: String = "",
    @SerializedName("thumbnailUrl") val thumbnailUrl: String,
    val url: String
)

data class CurseForgeFile(
    val id: Int,
    @SerializedName("gameId") val gameId: Int,
    @SerializedName("modId") val modId: Int,
    @SerializedName("isAvailable") val isAvailable: Boolean,
    @SerializedName("displayName") val displayName: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("releaseType") val releaseType: Int,  // 1=release, 2=beta, 3=alpha
    @SerializedName("fileStatus") val fileStatus: Int,
    @SerializedName("downloadUrl") val downloadUrl: String?,
    @SerializedName("fileDate") val fileDate: String = "",
    @SerializedName("fileLength") val fileLength: Long = 0,
    @SerializedName("downloadCount") val downloadCount: Long,
    @SerializedName("gameVersions") val gameVersions: List<String> = emptyList()
)
