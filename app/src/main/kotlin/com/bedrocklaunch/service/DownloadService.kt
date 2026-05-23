package com.bedrocklaunch.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.bedrocklaunch.R
import com.bedrocklaunch.model.DownloadStatus
import com.bedrocklaunch.repository.DownloadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named

/**
 * Foreground service that processes the download queue one-by-one,
 * posting progress notifications and updating Room via [DownloadRepository].
 */
@AndroidEntryPoint
class DownloadService : Service() {

    @Inject lateinit var repo: DownloadRepository
    @Inject @Named("base") lateinit var okHttp: OkHttpClient

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null

    companion object {
        const val CHANNEL_ID = "download_channel"
        const val NOTIFICATION_ID = 1001
        const val ACTION_CANCEL = "com.bedrocklaunch.CANCEL_DOWNLOAD"
        const val EXTRA_DOWNLOAD_ID = "download_id"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Starting downloads…", 0))
        startDownloadLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_CANCEL) {
            val id = intent.getStringExtra(EXTRA_DOWNLOAD_ID)
            if (id != null) serviceScope.launch { repo.cancelDownload(id) }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    // ── Download loop ─────────────────────────────────────

    private fun startDownloadLoop() {
        downloadJob = serviceScope.launch {
            while (isActive) {
                val active = repo.getActiveDownloads().first()
                val pending = active.filter { it.status == DownloadStatus.PENDING }
                if (pending.isEmpty()) {
                    delay(2000)
                    continue
                }
                val item = pending.first()
                downloadFile(item.id, item.name, item.url, item.destPath)
            }
        }
    }

    private suspend fun downloadFile(id: String, name: String, url: String, destPath: String) {
        try {
            updateNotification("Downloading $name…", 0)
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "BedrockLaunch/1.0")
                .build()

            okHttp.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    repo.markFailed(id, "HTTP ${response.code}")
                    return
                }
                val body = response.body ?: run {
                    repo.markFailed(id, "Empty response body")
                    return
                }
                val totalBytes = body.contentLength()
                val destFile = File(destPath)
                destFile.parentFile?.mkdirs()

                var downloadedBytes = 0L
                body.byteStream().use { input ->
                    FileOutputStream(destFile).use { output ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                            downloadedBytes += read
                            repo.updateProgress(id, downloadedBytes, totalBytes)
                            val progress = if (totalBytes > 0)
                                ((downloadedBytes * 100) / totalBytes).toInt()
                            else 0
                            updateNotification("Downloading $name…", progress)
                        }
                    }
                }
                repo.markCompleted(id)
                updateNotification("$name — complete", 100)
            }
        } catch (e: IOException) {
            repo.markFailed(id, e.message ?: "IO error")
        }
    }

    // ── Notifications ──────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Downloads",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "BedrockLaunch download progress" }
        getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }

    private fun buildNotification(text: String, progress: Int): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("BedrockLaunch")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setProgress(100, progress, progress == 0)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(text: String, progress: Int) {
        val nm = getSystemService(NotificationManager::class.java) ?: return
        nm.notify(NOTIFICATION_ID, buildNotification(text, progress))
    }
}
