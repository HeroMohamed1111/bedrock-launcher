package com.bedrocklaunch.repository

import com.bedrocklaunch.data.dao.InstalledPackDao
import com.bedrocklaunch.data.dao.ProfileDao
import com.bedrocklaunch.data.entity.InstalledPackEntity
import com.bedrocklaunch.data.entity.ProfileEntity
import com.bedrocklaunch.model.*
import com.bedrocklaunch.util.MinecraftDetector
import com.bedrocklaunch.util.PackScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MinecraftRepository @Inject constructor(
    private val detector: MinecraftDetector,
    private val profileDao: ProfileDao,
    private val packDao: InstalledPackDao,
    private val packScanner: PackScanner
) {

    // ── Minecraft detection ──────────────────────────────

    suspend fun getMinecraftInfo(): MinecraftInfo? = withContext(Dispatchers.IO) {
        detector.getMinecraftInfo()
    }

    fun launchMinecraft(): Boolean = detector.launchMinecraft()

    fun openPlayStore() = detector.openPlayStore()

    // ── Profiles ─────────────────────────────────────────

    fun getAllProfiles(): Flow<List<Profile>> =
        profileDao.getAllProfiles().map { list -> list.map { it.toModel() } }

    suspend fun getActiveProfile(): Profile? =
        profileDao.getActiveProfile()?.toModel()

    suspend fun createProfile(name: String, notes: String = ""): Profile {
        val profile = ProfileEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            notes = notes
        )
        profileDao.insert(profile)
        return profile.toModel()
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.update(profile.toEntity())
    }

    suspend fun deleteProfile(profile: Profile) {
        profileDao.delete(profile.toEntity())
    }

    suspend fun setActiveProfile(id: String) {
        profileDao.clearActiveProfile()
        profileDao.setActiveProfile(id)
    }

    // ── Packs ─────────────────────────────────────────────

    fun getAllPacks(): Flow<List<Pack>> =
        packDao.getAllPacks().map { list -> list.map { it.toModel() } }

    fun getPacksByType(type: PackType): Flow<List<Pack>> =
        packDao.getPacksByType(type.name).map { list -> list.map { it.toModel() } }

    /** Scan the filesystem and refresh the installed-packs DB table. */
    suspend fun refreshPacks() = withContext(Dispatchers.IO) {
        val scanned = packScanner.scanInstalledPacks()
        scanned.forEach { packDao.insert(it.toEntity()) }
    }

    suspend fun togglePack(uuid: String, enabled: Boolean) {
        packDao.setEnabled(uuid, enabled)
    }

    suspend fun deletePack(pack: Pack) {
        packDao.delete(pack.toEntity())
    }

    // ── Mapper helpers ────────────────────────────────────

    private fun ProfileEntity.toModel() = Profile(
        id = id, name = name, iconPath = iconPath,
        createdAt = createdAt, lastUsed = lastUsed,
        notes = notes, isActive = isActive
    )

    private fun Profile.toEntity() = ProfileEntity(
        id = id, name = name, iconPath = iconPath,
        createdAt = createdAt, lastUsed = lastUsed,
        notes = notes, isActive = isActive
    )

    private fun InstalledPackEntity.toModel() = Pack(
        uuid = uuid, name = name, description = description,
        version = version, author = author, iconPath = iconPath,
        path = path, type = PackType.valueOf(type), size = size, isEnabled = isEnabled
    )

    private fun Pack.toEntity() = InstalledPackEntity(
        uuid = uuid, name = name, description = description,
        version = version, author = author, iconPath = iconPath,
        path = path, type = type.name, size = size, isEnabled = isEnabled
    )
}
