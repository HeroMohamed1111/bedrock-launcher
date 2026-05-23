package com.bedrocklaunch.viewmodel

import androidx.lifecycle.*
import com.bedrocklaunch.model.*
import com.bedrocklaunch.repository.MinecraftRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LauncherViewModel @Inject constructor(
    private val repo: MinecraftRepository
) : ViewModel() {

    private val _mcInfo = MutableLiveData<MinecraftInfo?>()
    val mcInfo: LiveData<MinecraftInfo?> = _mcInfo

    private val _launchState = MutableLiveData<LaunchState>(LaunchState.Idle)
    val launchState: LiveData<LaunchState> = _launchState

    private val _activeProfile = MutableLiveData<Profile?>()
    val activeProfile: LiveData<Profile?> = _activeProfile

    val profiles: LiveData<List<Profile>> = repo.getAllProfiles().asLiveData()
    val allPacks: LiveData<List<Pack>> = repo.getAllPacks().asLiveData()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _mcInfo.value = repo.getMinecraftInfo()
            _activeProfile.value = repo.getActiveProfile()
            repo.refreshPacks()
        }
    }

    fun launchMinecraft() {
        _launchState.value = LaunchState.Launching
        val success = repo.launchMinecraft()
        _launchState.value = if (success) LaunchState.Launched else LaunchState.NotInstalled
    }

    fun openPlayStore() = repo.openPlayStore()

    fun createProfile(name: String, notes: String = "") {
        viewModelScope.launch { repo.createProfile(name, notes) }
    }

    fun deleteProfile(profile: Profile) {
        viewModelScope.launch { repo.deleteProfile(profile) }
    }

    fun setActiveProfile(profileId: String) {
        viewModelScope.launch {
            repo.setActiveProfile(profileId)
            _activeProfile.value = repo.getActiveProfile()
        }
    }

    fun togglePack(uuid: String, enabled: Boolean) {
        viewModelScope.launch { repo.togglePack(uuid, enabled) }
    }

    sealed class LaunchState {
        object Idle : LaunchState()
        object Launching : LaunchState()
        object Launched : LaunchState()
        object NotInstalled : LaunchState()
    }
}
