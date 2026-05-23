package com.bedrocklaunch.api.modrinth

import com.google.gson.annotations.SerializedName
import retrofit2.http.*

// ─────────────────────────────────────────────
// Modrinth REST API (v2) — Bedrock addons
// Base URL: https://api.modrinth.com/v2/
// ─────────────────────────────────────────────

interface ModrinthApi {

    /** Search projects (mods/resource packs for Bedrock). */
    @GET("search")
    suspend fun searchProjects(
        @Query("query") query: String = "",
        @Query("facets") facets: String = "[[\"categories:bedrock\"]]",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0,
        @Query("index") sortBy: String = "relevance"  // relevance | downloads | follows | newest | updated
    ): ModrinthSearchResponse

    /** Get a single project by slug or id. */
    @GET("project/{id}")
    suspend fun getProject(@Path("id") id: String): ModrinthProject

    /** Get all versions of a project. */
    @GET("project/{id}/version")
    suspend fun getProjectVersions(
        @Path("id") id: String,
        @Query("loaders") loaders: String? = null,
        @Query("game_versions") gameVersions: String? = null
    ): List<ModrinthVersion>

    /** Batch fetch multiple projects. */
    @GET("projects")
    suspend fun getProjects(@Query("ids") ids: String): List<ModrinthProject>
}

// ─────────────────────────────────────────────
// Response models
// ─────────────────────────────────────────────

data class ModrinthSearchResponse(
    val hits: List<ModrinthSearchHit>,
    @SerializedName("total_hits") val totalHits: Int,
    val offset: Int,
    val limit: Int
)

data class ModrinthSearchHit(
    @SerializedName("project_id") val projectId: String,
    val slug: String,
    val title: String,
    val description: String,
    val author: String,
    @SerializedName("display_categories") val displayCategories: List<String> = emptyList(),
    val versions: List<String> = emptyList(),
    val follows: Int = 0,
    @SerializedName("date_created") val dateCreated: String = "",
    @SerializedName("date_modified") val dateModified: String = "",
    @SerializedName("latest_version") val latestVersion: String? = null,
    val license: String? = null,
    @SerializedName("client_side") val clientSide: String = "",
    @SerializedName("server_side") val serverSide: String = "",
    @SerializedName("icon_url") val iconUrl: String? = null,
    val downloads: Int = 0,
    @SerializedName("color") val color: Int? = null,
    @SerializedName("gallery") val gallery: List<String> = emptyList()
)

data class ModrinthProject(
    val id: String,
    val slug: String,
    val title: String,
    val description: String,
    val body: String = "",
    val categories: List<String> = emptyList(),
    @SerializedName("additional_categories") val additionalCategories: List<String> = emptyList(),
    @SerializedName("icon_url") val iconUrl: String? = null,
    val versions: List<String> = emptyList(),
    val downloads: Int = 0,
    val followers: Int = 0,
    val updated: String = "",
    val published: String = "",
    val gallery: List<ModrinthGalleryItem> = emptyList(),
    val license: ModrinthLicense? = null
)

data class ModrinthVersion(
    val id: String,
    val name: String,
    @SerializedName("version_number") val versionNumber: String,
    val changelog: String = "",
    @SerializedName("date_published") val datePublished: String = "",
    val downloads: Int = 0,
    @SerializedName("version_type") val versionType: String = "release",
    @SerializedName("game_versions") val gameVersions: List<String> = emptyList(),
    val files: List<ModrinthFile> = emptyList()
)

data class ModrinthFile(
    val url: String,
    val filename: String,
    val primary: Boolean = false,
    val size: Long = 0
)

data class ModrinthGalleryItem(
    val url: String,
    val featured: Boolean = false,
    val title: String? = null,
    val description: String? = null
)

data class ModrinthLicense(
    val id: String,
    val name: String,
    val url: String? = null
)
