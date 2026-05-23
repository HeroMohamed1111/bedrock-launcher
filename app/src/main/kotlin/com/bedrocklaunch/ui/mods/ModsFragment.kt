package com.bedrocklaunch.ui.mods

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.FragmentModsBinding
import com.bedrocklaunch.model.ModSource
import com.bedrocklaunch.model.Result
import com.bedrocklaunch.viewmodel.ModsViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ModsFragment : Fragment() {

    private var _binding: FragmentModsBinding? = null
    private val binding get() = _binding!!
    private val vm: ModsViewModel by viewModels()
    private lateinit var modAdapter: ModAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentModsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecycler()
        setupTabs()
        setupSearch()
        setupObservers()
    }

    private fun setupRecycler() {
        modAdapter = ModAdapter(
            onModClick = { mod ->
                vm.selectMod(mod)
                findNavController().navigate(R.id.action_modsFragment_to_modDetailFragment)
            },
            onBookmark = { mod -> vm.toggleBookmark(mod) }
        )
        binding.rvMods.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = modAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                    val lm = rv.layoutManager as LinearLayoutManager
                    if (lm.findLastVisibleItemPosition() >= modAdapter.itemCount - 5) {
                        vm.loadNextPage()
                    }
                }
            })
        }
    }

    private fun setupTabs() {
        binding.tabSource.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val source = when (tab.position) {
                    0 -> ModSource.MODRINTH
                    1 -> ModSource.CURSEFORGE
                    else -> ModSource.MODRINTH
                }
                vm.switchSource(source)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                vm.search(query)
                return true
            }
            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.isEmpty()) vm.search("")
                return false
            }
        })
    }

    private fun setupObservers() {
        vm.mods.observe(viewLifecycleOwner) { result ->
            when (result) {
                is Result.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvMods.visibility = View.GONE
                    binding.tvEmpty.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (result.data.isEmpty()) {
                        binding.rvMods.visibility = View.GONE
                        binding.tvEmpty.visibility = View.VISIBLE
                    } else {
                        binding.rvMods.visibility = View.VISIBLE
                        binding.tvEmpty.visibility = View.GONE
                        modAdapter.submitList(result.data)
                    }
                }
                is Result.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.rvMods.visibility = View.VISIBLE
                    Snackbar.make(binding.root, "Error: ${result.message}", Snackbar.LENGTH_LONG)
                        .setAction("Retry") { vm.search("") }
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
