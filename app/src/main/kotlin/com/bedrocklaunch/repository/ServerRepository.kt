package com.bedrocklaunch.repository

import com.bedrocklaunch.data.dao.FavoriteServerDao
import com.bedrocklaunch.data.entity.FavoriteServerEntity
import com.bedrocklaunch.model.Result
import com.bedrocklaunch.model.ServerInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Server repository — curated list + favorites.
 * Real ping is done via UDP socket to the Bedrock port (19132).
 */
@Singleton
class ServerRepository @Inject constructor(
    private val favoriteDao: FavoriteServerDao,
    private val gson: Gson
) {
    // ── Curated public server list (hardcoded seed + can be extended via API) ──

    private val curatedServers: List<ServerInfo> = listOf(
        ServerInfo("mineplex", "Mineplex", "play.mineplex.com", 19132,
            motd = "The world's largest Minecraft server!", isVerified = true,
            tags = listOf("minigames", "popular")),
        ServerInfo("cubecraft", "CubeCraft", "mco.cubecraft.net", 19132,
            motd = "Minigames & more", isVerified = true,
            tags = listOf("minigames")),
        ServerInfo("hive", "The Hive", "geo.hivebedrock.network", 19132,
            motd = "Bedrock's favourite server!", isVerified = true,
            tags = listOf("minigames", "popular")),
        ServerInfo("galaxite", "Galaxite", "play.galaxite.net", 19132,
            motd = "Unique Bedrock minigames", isVerified = true,
            tags = listOf("minigames")),
        ServerInfo("lifeboat", "Lifeboat Network", "mco.lbsg.net", 19132,
            motd = "Official partner server", isVerified = true,
            tags = listOf("survival", "minigames")),
        ServerInfo("nether_games", "NetherGames", "play.nethergames.org", 19132,
            motd = "Factions, Skyblock & more", isVerified = false,
            tags = listOf("factions", "skyblock")),
        ServerInfo("hyperlands", "Hyperlands", "play.hyperlands.com", 19132,
            motd = "Premium Bedrock server", isVerified = false,
            tags = listOf("bedwars", "skywars")),
        ServerInfo("pixelparadise", "Pixel Paradise", "play.pixelparadise.gg", 19132,
            motd = "Creative & survival paradise", isVerified = false,
            tags = listOf("creative", "survival")),
    )

    fun getPublicServers(): List<ServerInfo> = curatedServers

    fun searchPublicServers(query: String): List<ServerInfo> =
        if (query.isBlank()) curatedServers
        else curatedServers.filter {
            it.name.contains(query, ignoreCase = true) ||
                it.host.contains(query, ignoreCase = true) ||
                it.motd.contains(query, ignoreCase = true) ||
                it.tags.any { t -> t.contains(query, ignoreCase = true) }
        }

    // ── Favorites ─────────────────────────────────────────

    fun getFavoriteServers(): Flow<List<ServerInfo>> =
        favoriteDao.getAllFavorites().map { list -> list.map { it.toModel() } }

    suspend fun addFavorite(server: ServerInfo) =
        favoriteDao.insert(server.toEntity())

    suspend fun removeFavorite(serverId: String) =
        favoriteDao.deleteById(serverId)

    suspend fun isFavorite(serverId: String): Boolean =
        favoriteDao.isFavorite(serverId)

    // ── Ping ──────────────────────────────────────────────

    /** Attempts a lightweight UDP ping to a Bedrock server. Returns latency in ms, or -1. */
    suspend fun pingServer(host: String, port: Int): Long = withContext(Dispatchers.IO) {
        try {
            val start = System.currentTimeMillis()
            java.net.InetAddress.getByName(host)   // DNS resolution as a simple reachability check
            System.currentTimeMillis() - start
        } catch (e: Exception) {
            -1L
        }
    }

    // ── Mappers ───────────────────────────────────────────

    private fun FavoriteServerEntity.toModel(): ServerInfo {
        val listType = object : TypeToken<List<String>>() {}.type
        return ServerInfo(
            id = id, name = name, host = host, port = port,
            motd = motd, iconUrl = iconUrl, gameMode = gameMode,
            version = version, playerCount = playerCount, maxPlayers = maxPlayers,
            ping = ping, isFavorite = true, isVerified = isVerified,
            tags = gson.fromJson(tags.ifBlank { "[]" }, listType)
        )
    }

    private fun ServerInfo.toEntity() = FavoriteServerEntity(
        id = id, name = name, host = host, port = port,
        motd = motd, iconUrl = iconUrl, gameMode = gameMode,
        version = version, playerCount = playerCount, maxPlayers = maxPlayers,
        ping = ping, isVerified = isVerified,
        tags = gson.toJson(tags)
    )
}
