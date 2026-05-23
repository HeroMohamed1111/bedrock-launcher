package com.bedrocklaunch.viewmodel

import androidx.lifecycle.*
import com.bedrocklaunch.model.ServerInfo
import com.bedrocklaunch.repository.ServerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ServersViewModel @Inject constructor(
    private val repo: ServerRepository
) : ViewModel() {

    private val _query = MutableLiveData("")
    val filteredServers: LiveData<List<ServerInfo>> = _query.map { q ->
        repo.searchPublicServers(q)
    }

    val favoriteServers: LiveData<List<ServerInfo>> = repo.getFavoriteServers().asLiveData()

    private val _pingResults = MutableLiveData<Map<String, Long>>(emptyMap())
    val pingResults: LiveData<Map<String, Long>> = _pingResults

    fun setQuery(q: String) { _query.value = q }

    fun pingAll(servers: List<ServerInfo>) {
        viewModelScope.launch {
            val results = mutableMapOf<String, Long>()
            servers.forEach { server ->
                results[server.id] = repo.pingServer(server.host, server.port)
            }
            _pingResults.value = results
        }
    }

    fun pingServer(server: ServerInfo) {
        viewModelScope.launch {
            val latency = repo.pingServer(server.host, server.port)
            val current = _pingResults.value?.toMutableMap() ?: mutableMapOf()
            current[server.id] = latency
            _pingResults.value = current
        }
    }

    fun addFavorite(server: ServerInfo) {
        viewModelScope.launch { repo.addFavorite(server) }
    }

    fun removeFavorite(serverId: String) {
        viewModelScope.launch { repo.removeFavorite(serverId) }
    }
}
