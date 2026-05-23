package com.bedrocklaunch.util

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import com.bedrocklaunch.model.MinecraftInfo
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Detects Minecraft Bedrock installation and launches it via Android intent.
 */
@Singleton
class MinecraftDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val MC_PACKAGE = "com.mojang.minecraftpe"
    }

    /** Returns info about the installed Minecraft Bedrock, or null if not installed. */
    fun getMinecraftInfo(): MinecraftInfo? {
        return try {
            val pm = context.packageManager
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(MC_PACKAGE, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(MC_PACKAGE, 0)
            }
            val appInfo = packageInfo.applicationInfo
            MinecraftInfo(
                packageName = MC_PACKAGE,
                versionName = packageInfo.versionName ?: "Unknown",
                versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    packageInfo.longVersionCode
                else
                    @Suppress("DEPRECATION")
                    packageInfo.versionCode.toLong(),
                installPath = appInfo?.sourceDir ?: "",
                dataPath = appInfo?.dataDir ?: "",
                isInstalled = true,
                lastUpdated = packageInfo.lastUpdateTime
            )
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    /** True if Minecraft Bedrock is installed. */
    fun isInstalled(): Boolean = getMinecraftInfo() != null

    /**
     * Launch Minecraft Bedrock via the launcher intent.
     * Returns true if the intent was dispatched successfully.
     */
    fun launchMinecraft(): Boolean {
        return try {
            val intent = context.packageManager
                .getLaunchIntentForPackage(MC_PACKAGE)
                ?.apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            if (intent != null) {
                context.startActivity(intent)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Open the Play Store page for Minecraft Bedrock so users can install/update it.
     */
    fun openPlayStore() {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse("market://details?id=$MC_PACKAGE")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback: open in browser
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(
                    "https://play.google.com/store/apps/details?id=$MC_PACKAGE"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    /** Returns Minecraft's external data directory (packs, worlds, etc.). */
    fun getMinecraftDataDir(): java.io.File? {
        val externalDirs = context.getExternalFilesDirs(null)
        for (dir in externalDirs) {
            if (dir != null) {
                // Navigate to com.mojang data directory
                val mcDir = java.io.File(
                    dir.absolutePath.substringBefore("Android") +
                        "Android/data/$MC_PACKAGE/files/games/com.mojang"
                )
                if (mcDir.exists()) return mcDir
            }
        }
        return null
    }
}
