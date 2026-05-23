package com.bedrocklaunch.repository

import com.bedrocklaunch.api.curseforge.CurseForgeApi
import com.bedrocklaunch.api.curseforge.CurseForgeMod
import com.bedrocklaunch.api.modrinth.ModrinthApi
import com.bedrocklaunch.api.modrinth.ModrinthSearchHit
import com.bedrocklaunch.data.dao.CachedModDao
import com.bedrocklaunch.data.entity.CachedModEntity
import com.bedrocklaunch.model.Mod
import com.bedrocklaunch.model.ModSource
import com.bedrocklaunch.model.Result
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ModRepository @Inject constructor(
    private val modrinthApi: ModrinthApi,
    private val curseForgeApi: CurseForgeApi,
    private val modDao: CachedModDao,
    private val gson: Gson
) {

    // ── Modrinth ──────────────────────────────────────────

    suspend fun searchModrinth(
        query: String = "",
        offset: Int = 0,
        limit: Int = 20
    ): Result<List<Mod>> = runCatching {
        val resp = modrinthApi.searchProjects(
            query = query,
            offset = offset,
            limit = limit
        )
        resp.hits.map { it.toMod() }
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it) }
    )

    suspend fun getModrinthProjectDetail(id: String): Result<Mod> = runCatching {
        val project = modrinthApi.getProject(id)
        val versions = modrinthApi.getProjectVersions(id)
        val latestVersion = versions.firstOrNull()?.versionNumber ?: ""
        val downloadUrl = versions.firstOrNull()?.files?.firstOrNull { it.primary }?.url
            ?: versions.firstOrNull()?.files?.firstOrNull()?.url ?: ""
        val fileSize = versions.firstOrNull()?.files?.firstOrNull { it.primary }?.size ?: 0
        Mod(
            id = project.id,
            name = project.title,
            summary = project.description,
            description = project.body,
            author = "",
            downloadCount = project.downloads.toLong(),
            iconUrl = project.iconUrl,
            screenshots = project.gallery.map { it.url },
            latestVersion = latestVersion,
            gameVersions = versions.firstOrNull()?.gameVersions ?: emptyList(),
            categories = project.categories,
            downloadUrl = downloadUrl,
            fileSize = fileSize,
            source = ModSource.MODRINTH,
            projectUrl = "https://modrinth.com/project/${project.slug}"
        )
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it) }
    )

    // ── CurseForge ────────────────────────────────────────

    suspend fun searchCurseForge(
        query: String = "",
        index: Int = 0,
        pageSize: Int = 20
    ): Result<List<Mod>> = runCatching {
        val resp = curseForgeApi.searchMods(
            query = query,
            index = index,
            pageSize = pageSize
        )
        resp.data.map { it.toMod() }
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Error(it) }
    )

    // ── Bookmarks ─────────────────────────────────────────

    fun getBookmarkedMods(): Flow<List<Mod>> =
        modDao.getBookmarkedMods().map { list -> list.map { it.toMod() } }

    suspend fun setBookmarked(mod: Mod, bookmarked: Boolean) {
        if (bookmarked) {
            modDao.insert(mod.toCachedEntity(bookmarked = true))
        } else {
            modDao.setBookmarked(mod.id, false)
        }
    }

    // ── Mappers ───────────────────────────────────────────

    private fun ModrinthSearchHit.toMod() = Mod(
        id = projectId,
        name = title,
        summary = description,
        author = author,
        downloadCount = downloads.toLong(),
        iconUrl = iconUrl,
        latestVersion = latestVersion ?: "",
        gameVersions = versions,
        categories = displayCategories,
        source = ModSource.MODRINTH,
        projectUrl = "https://modrinth.com/project/$slug"
    )

    private fun CurseForgeMod.toMod() = Mod(
        id = id.toString(),
        name = name,
        summary = summary,
        author = authors.firstOrNull()?.name ?: "",
        downloadCount = downloadCount,
        iconUrl = logo?.thumbnailUrl,
        screenshots = screenshots.map { it.url },
        latestVersion = latestFiles.firstOrNull()?.displayName ?: "",
        gameVersions = latestFiles.firstOrNull()?.gameVersions ?: emptyList(),
        downloadUrl = latestFiles.firstOrNull()?.downloadUrl ?: "",
        fileSize = latestFiles.firstOrNull()?.fileLength ?: 0,
        source = ModSource.CURSEFORGE,
        projectUrl = links.websiteUrl
    )

    private fun CachedModEntity.toMod(): Mod {
        val listType = object : TypeToken<List<String>>() {}.type
        return Mod(
            id = id,
            name = name,
            summary = summary,
            author = author,
            downloadCount = downloadCount,
            rating = rating,
            iconUrl = iconUrl,
            latestVersion = latestVersion,
            gameVersions = gson.fromJson(gameVersions.ifBlank { "[]" }, listType),
            categories = gson.fromJson(categories.ifBlank { "[]" }, listType),
            downloadUrl = downloadUrl,
            fileSize = fileSize,
            source = ModSource.valueOf(source),
            projectUrl = projectUrl,
            updatedAt = updatedAt
        )
    }

    private fun Mod.toCachedEntity(bookmarked: Boolean = false) = CachedModEntity(
        id = id,
        name = name,
        summary = summary,
        author = author,
        downloadCount = downloadCount,
        rating = rating,
        iconUrl = iconUrl,
        latestVersion = latestVersion,
        gameVersions = gson.toJson(gameVersions),
        categories = gson.toJson(categories),
        downloadUrl = downloadUrl,
        fileSize = fileSize,
        source = source.name,
        projectUrl = projectUrl,
        updatedAt = updatedAt,
        isBookmarked = bookmarked
    )
}
