package com.bedrocklaunch.viewmodel

import androidx.lifecycle.*
import com.bedrocklaunch.model.DownloadItem
import com.bedrocklaunch.repository.DownloadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadsViewModel @Inject constructor(
    private val repo: DownloadRepository
) : ViewModel() {

    val allDownloads: LiveData<List<DownloadItem>> = repo.getAllDownloads().asLiveData()
    val activeDownloads: LiveData<List<DownloadItem>> = repo.getActiveDownloads().asLiveData()

    fun cancelDownload(id: String) {
        viewModelScope.launch { repo.cancelDownload(id) }
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch { repo.deleteDownload(id) }
    }

    fun clearFinished() {
        viewModelScope.launch { repo.clearFinished() }
    }
}
