package com.bedrocklaunch.repository

import com.bedrocklaunch.model.NewsArticle
import com.bedrocklaunch.model.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches Bedrock community news from public RSS/JSON feeds.
 * Currently uses Minecraft.net official feed (JSON API).
 */
@Singleton
class NewsRepository @Inject constructor() {

    private val feeds = listOf(
        "https://www.minecraft.net/content/dam/games/minecraft/articles.json" to "Minecraft.net",
    )

    suspend fun fetchNews(page: Int = 0): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            // Minecraft.net articles endpoint
            val articles = mutableListOf<NewsArticle>()
            val url = "https://www.minecraft.net/content/dam/games/minecraft/articles.json"
            val json = URL(url).readText(Charsets.UTF_8)
            val root = JSONObject(json)
            val articleArray = root.optJSONObject("article_grid")
                ?.optJSONArray("article_grid") ?: JSONArray()

            for (i in 0 until articleArray.length()) {
                val item = articleArray.getJSONObject(i)
                val dateStr = item.optString("publish_date", "")
                articles.add(
                    NewsArticle(
                        id = item.optString("default_tile.title", "").hashCode().toString(),
                        title = item.optString("default_tile.title", "No title"),
                        summary = item.optString("default_tile.sub_header", ""),
                        imageUrl = item.optJSONObject("default_tile.image.imageURL")
                            ?.optString("url")
                            ?: item.optString("default_tile.image.imageURL", null),
                        publishedAt = System.currentTimeMillis(), // simplified date
                        url = "https://www.minecraft.net" + item.optString("article_url", ""),
                        source = "Minecraft.net",
                        tags = listOf(item.optString("primary_tag", ""))
                    )
                )
            }
            Result.Success(articles.take(30))
        } catch (e: Exception) {
            // Return curated fallback articles when offline
            Result.Success(getFallbackArticles())
        }
    }

    private fun getFallbackArticles(): List<NewsArticle> = listOf(
        NewsArticle(
            id = "1",
            title = "Minecraft Bedrock 1.21 — Tricky Trials Update",
            summary = "The Tricky Trials update brings the Trial Chamber, new mobs, and the Wind Charge to Bedrock Edition.",
            imageUrl = null,
            publishedAt = 1717200000000L,
            url = "https://www.minecraft.net/en-us/article/tricky-trials-out-now",
            source = "Minecraft.net",
            tags = listOf("update")
        ),
        NewsArticle(
            id = "2",
            title = "Marketplace Content Spotlight",
            summary = "Check out the latest Marketplace packs, skins, and worlds from the Bedrock creator community.",
            imageUrl = null,
            publishedAt = 1716595200000L,
            url = "https://www.minecraft.net/en-us/marketplace",
            source = "Minecraft.net",
            tags = listOf("marketplace")
        ),
        NewsArticle(
            id = "3",
            title = "Realm vs Server — What's Right for You?",
            summary = "Learn about the different ways to play Minecraft Bedrock with friends online.",
            imageUrl = null,
            publishedAt = 1715990400000L,
            url = "https://www.minecraft.net/en-us/article/realms",
            source = "Minecraft.net",
            tags = listOf("guides")
        ),
    )
}
