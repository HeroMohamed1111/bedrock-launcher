package com.bedrocklaunch.ui.servers

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.FragmentServersBinding
import com.bedrocklaunch.model.ServerInfo
import com.bedrocklaunch.viewmodel.ServersViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ServersFragment : Fragment() {
    private var _b: FragmentServersBinding? = null
    private val b get() = _b!!
    private val vm: ServersViewModel by viewModels()
    private lateinit var serverAdapter: ServerAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentServersBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serverAdapter = ServerAdapter(
            onJoin = { server -> joinServer(server) },
            onFavorite = { server, fav ->
                if (fav) vm.addFavorite(server) else vm.removeFavorite(server.id)
            },
            onPing = { server -> vm.pingServer(server) }
        )
        b.rvServers.layoutManager = LinearLayoutManager(requireContext())
        b.rvServers.adapter = serverAdapter

        b.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean { vm.setQuery(q); return true }
            override fun onQueryTextChange(t: String): Boolean { vm.setQuery(t); return false }
        })

        b.tabServers.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) { updateList(tab.position) }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        vm.filteredServers.observe(viewLifecycleOwner) { updateList(b.tabServers.selectedTabPosition) }
        vm.favoriteServers.observe(viewLifecycleOwner) { updateList(b.tabServers.selectedTabPosition) }
        vm.pingResults.observe(viewLifecycleOwner) { pings ->
            serverAdapter.updatePings(pings)
        }

        vm.pingAll(vm.filteredServers.value ?: emptyList())
    }

    private fun updateList(tabPos: Int) {
        val list = if (tabPos == 1) vm.favoriteServers.value ?: emptyList()
                   else vm.filteredServers.value ?: emptyList()
        serverAdapter.submitList(list)
        b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun joinServer(server: ServerInfo) {
        Snackbar.make(b.root,
            "To join: add ${server.host}:${server.port} in Minecraft's server list",
            Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
