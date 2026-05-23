package com.bedrocklaunch.repository

import com.bedrocklaunch.data.dao.DownloadDao
import com.bedrocklaunch.data.entity.DownloadEntity
import com.bedrocklaunch.model.DownloadItem
import com.bedrocklaunch.model.DownloadStatus
import com.bedrocklaunch.model.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    fun getAllDownloads(): Flow<List<DownloadItem>> =
        downloadDao.getAllDownloads().map { list -> list.map { it.toModel() } }

    fun getActiveDownloads(): Flow<List<DownloadItem>> =
        downloadDao.getActiveDownloads().map { list -> list.map { it.toModel() } }

    suspend fun enqueueDownload(
        name: String,
        url: String,
        destPath: String,
        mimeType: String = ""
    ): String {
        val id = UUID.randomUUID().toString()
        downloadDao.insert(
            DownloadEntity(
                id = id,
                name = name,
                url = url,
                destPath = destPath,
                mimeType = mimeType,
                status = DownloadStatus.PENDING.name
            )
        )
        return id
    }

    suspend fun updateProgress(id: String, bytes: Long, total: Long) {
        val progress = if (total > 0) ((bytes * 100) / total).toInt() else 0
        downloadDao.updateProgress(id, bytes, progress, DownloadStatus.RUNNING.name)
    }

    suspend fun markCompleted(id: String) {
        downloadDao.updateStatus(
            id,
            DownloadStatus.COMPLETED.name,
            completedAt = System.currentTimeMillis()
        )
    }

    suspend fun markFailed(id: String, error: String) {
        downloadDao.updateStatus(id, DownloadStatus.FAILED.name, error = error)
    }

    suspend fun cancelDownload(id: String) {
        downloadDao.updateStatus(id, DownloadStatus.CANCELLED.name)
    }

    suspend fun deleteDownload(id: String) {
        val entity = downloadDao.getById(id) ?: return
        downloadDao.delete(entity)
    }

    suspend fun clearFinished() = downloadDao.clearFinished()

    private fun DownloadEntity.toModel() = DownloadItem(
        id = id,
        name = name,
        url = url,
        destPath = destPath,
        totalBytes = totalBytes,
        downloadedBytes = downloadedBytes,
        status = DownloadStatus.valueOf(status),
        progress = progress,
        errorMessage = errorMessage,
        mimeType = mimeType,
        createdAt = createdAt,
        completedAt = completedAt
    )
}
