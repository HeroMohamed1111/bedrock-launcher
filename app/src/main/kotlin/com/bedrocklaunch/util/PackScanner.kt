package com.bedrocklaunch.util

import android.content.Context
import com.bedrocklaunch.model.Pack
import com.bedrocklaunch.model.PackType
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Scans Minecraft's data directories for installed resource/behaviour packs and worlds.
 */
@Singleton
class PackScanner @Inject constructor(
    @ApplicationContext private val context: Context,
    private val detector: MinecraftDetector
) {
    suspend fun scanInstalledPacks(): List<Pack> = withContext(Dispatchers.IO) {
        val result = mutableListOf<Pack>()
        val mcDir = detector.getMinecraftDataDir() ?: return@withContext result

        // Resource packs
        result += scanPackDir(File(mcDir, "resource_packs"), PackType.RESOURCE)
        // Behaviour packs
        result += scanPackDir(File(mcDir, "behavior_packs"), PackType.BEHAVIOR)
        // World packs
        result += scanPackDir(File(mcDir, "minecraftWorlds"), PackType.WORLD)

        result
    }

    private fun scanPackDir(dir: File, type: PackType): List<Pack> {
        if (!dir.exists() || !dir.isDirectory) return emptyList()
        val packs = mutableListOf<Pack>()

        dir.listFiles()?.forEach { entry ->
            when {
                entry.isDirectory -> {
                    val manifest = File(entry, "manifest.json")
                    if (manifest.exists()) {
                        parseManifest(manifest, entry.absolutePath, type, entry.length())
                            ?.let { packs.add(it) }
                    }
                }
                entry.extension == "mcpack" || entry.extension == "mcaddon" -> {
                    parseMcPack(entry, type)?.let { packs.add(it) }
                }
            }
        }
        return packs
    }

    private fun parseManifest(manifest: File, path: String, type: PackType, size: Long): Pack? {
        return try {
            val json = JSONObject(manifest.readText())
            val header = json.getJSONObject("header")
            val versionArr = header.optJSONArray("version")
            val version = if (versionArr != null) {
                (0 until versionArr.length()).joinToString(".") { versionArr.getInt(it).toString() }
            } else ""
            Pack(
                uuid = header.optString("uuid", path.hashCode().toString()),
                name = header.optString("name", manifest.parentFile?.name ?: "Unknown"),
                description = header.optString("description", ""),
                version = version,
                path = path,
                type = type,
                size = size
            )
        } catch (e: Exception) { null }
    }

    private fun parseMcPack(file: File, type: PackType): Pack? {
        return try {
            ZipFile(file).use { zip ->
                val manifestEntry = zip.getEntry("manifest.json") ?: return null
                val json = JSONObject(zip.getInputStream(manifestEntry).bufferedReader().readText())
                val header = json.getJSONObject("header")
                Pack(
                    uuid = header.optString("uuid", file.nameWithoutExtension),
                    name = header.optString("name", file.nameWithoutExtension),
                    description = header.optString("description", ""),
                    path = file.absolutePath,
                    type = type,
                    size = file.length()
                )
            }
        } catch (e: Exception) { null }
    }
}
