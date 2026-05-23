package com.bedrocklaunch.viewmodel

import androidx.lifecycle.*
import com.bedrocklaunch.model.Mod
import com.bedrocklaunch.model.ModSource
import com.bedrocklaunch.model.Result
import com.bedrocklaunch.repository.ModRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ModsViewModel @Inject constructor(
    private val repo: ModRepository
) : ViewModel() {

    private val _mods = MutableLiveData<Result<List<Mod>>>(Result.Loading)
    val mods: LiveData<Result<List<Mod>>> = _mods

    private val _selectedMod = MutableLiveData<Mod?>()
    val selectedMod: LiveData<Mod?> = _selectedMod

    val bookmarkedMods: LiveData<List<Mod>> = repo.getBookmarkedMods().asLiveData()

    private var currentQuery = ""
    private var currentSource = ModSource.MODRINTH
    private var currentOffset = 0
    private val pageSize = 20
    private val allLoadedMods = mutableListOf<Mod>()
    private var searchJob: Job? = null

    init { search("", ModSource.MODRINTH) }

    fun search(query: String, source: ModSource = currentSource) {
        searchJob?.cancel()
        currentQuery = query
        currentSource = source
        currentOffset = 0
        allLoadedMods.clear()
        _mods.value = Result.Loading

        searchJob = viewModelScope.launch {
            val result = when (source) {
                ModSource.MODRINTH -> repo.searchModrinth(query = query, offset = 0, limit = pageSize)
                ModSource.CURSEFORGE -> repo.searchCurseForge(query = query, index = 0, pageSize = pageSize)
                ModSource.LOCAL -> Result.Success(emptyList())
            }
            if (result is Result.Success) {
                allLoadedMods.addAll(result.data)
                currentOffset = allLoadedMods.size
            }
            _mods.value = result
        }
    }

    fun loadNextPage() {
        if (_mods.value is Result.Loading) return
        viewModelScope.launch {
            val result = when (currentSource) {
                ModSource.MODRINTH -> repo.searchModrinth(query = currentQuery, offset = currentOffset, limit = pageSize)
                ModSource.CURSEFORGE -> repo.searchCurseForge(query = currentQuery, index = currentOffset, pageSize = pageSize)
                ModSource.LOCAL -> Result.Success(emptyList())
            }
            if (result is Result.Success && result.data.isNotEmpty()) {
                allLoadedMods.addAll(result.data)
                currentOffset = allLoadedMods.size
                _mods.value = Result.Success(allLoadedMods.toList())
            }
        }
    }

    fun selectMod(mod: Mod) {
        _selectedMod.value = mod
        if (mod.description.isBlank() && mod.source == ModSource.MODRINTH) {
            viewModelScope.launch {
                val detail = repo.getModrinthProjectDetail(mod.id)
                if (detail is Result.Success) _selectedMod.value = detail.data
            }
        }
    }

    fun toggleBookmark(mod: Mod) {
        viewModelScope.launch {
            val isBookmarked = bookmarkedMods.value?.any { it.id == mod.id } == true
            repo.setBookmarked(mod, !isBookmarked)
        }
    }

    fun switchSource(source: ModSource) {
        if (source != currentSource) search(currentQuery, source)
    }
}
