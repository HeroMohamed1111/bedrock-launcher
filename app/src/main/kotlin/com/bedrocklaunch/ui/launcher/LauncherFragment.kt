package com.bedrocklaunch.ui.launcher

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.FragmentLauncherBinding
import com.bedrocklaunch.model.Profile
import com.bedrocklaunch.viewmodel.LauncherViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherFragment : Fragment() {

    private var _binding: FragmentLauncherBinding? = null
    private val binding get() = _binding!!
    private val vm: LauncherViewModel by viewModels()
    private lateinit var profileAdapter: ProfileAdapter
    private lateinit var packAdapter: PackAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        _binding = FragmentLauncherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclers()
        setupObservers()
        setupListeners()
        vm.refresh()
    }

    private fun setupRecyclers() {
        profileAdapter = ProfileAdapter(
            onSelect = { profile -> vm.setActiveProfile(profile.id) },
            onDelete = { profile -> confirmDeleteProfile(profile) }
        )
        packAdapter = PackAdapter(
            onToggle = { pack, enabled -> vm.togglePack(pack.uuid, enabled) }
        )
        binding.rvProfiles.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvProfiles.adapter = profileAdapter

        binding.rvPacks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPacks.adapter = packAdapter
    }

    private fun setupObservers() {
        vm.mcInfo.observe(viewLifecycleOwner) { info ->
            if (info != null) {
                binding.tvVersion.text = "Minecraft ${info.versionName}"
                binding.btnLaunch.isEnabled = true
                binding.btnPlayStore.visibility = View.GONE
                binding.cardMinecraftStatus.setCardBackgroundColor(
                    requireContext().getColor(com.google.android.material.R.color.design_default_color_primary)
                )
            } else {
                binding.tvVersion.text = "Minecraft not installed"
                binding.btnLaunch.isEnabled = false
                binding.btnPlayStore.visibility = View.VISIBLE
            }
        }

        vm.activeProfile.observe(viewLifecycleOwner) { profile ->
            binding.tvActiveProfile.text = profile?.name ?: "No active profile"
        }

        vm.profiles.observe(viewLifecycleOwner) { profiles ->
            profileAdapter.submitList(profiles)
            binding.tvProfileCount.text = "${profiles.size} profile(s)"
        }

        vm.allPacks.observe(viewLifecycleOwner) { packs ->
            packAdapter.submitList(packs)
            binding.tvPackCount.text = "${packs.size} pack(s) installed"
            binding.groupPacks.visibility = if (packs.isEmpty()) View.GONE else View.VISIBLE
        }

        vm.launchState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is LauncherViewModel.LaunchState.Launching -> {
                    binding.btnLaunch.isEnabled = false
                    binding.btnLaunch.text = "Launching…"
                }
                is LauncherViewModel.LaunchState.Launched -> {
                    binding.btnLaunch.isEnabled = true
                    binding.btnLaunch.text = "Launch Minecraft"
                }
                is LauncherViewModel.LaunchState.NotInstalled -> {
                    binding.btnLaunch.isEnabled = false
                    binding.btnLaunch.text = "Launch Minecraft"
                    Snackbar.make(binding.root, "Minecraft is not installed", Snackbar.LENGTH_LONG)
                        .setAction("Install") { vm.openPlayStore() }
                        .show()
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.btnLaunch.setOnClickListener { vm.launchMinecraft() }

        binding.btnPlayStore.setOnClickListener { vm.openPlayStore() }

        binding.btnAddProfile.setOnClickListener { showCreateProfileDialog() }

        binding.btnRefreshPacks.setOnClickListener {
            vm.refresh()
            Snackbar.make(binding.root, "Scanning packs…", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun showCreateProfileDialog() {
        val editText = TextInputEditText(requireContext())
        editText.hint = "Profile name"
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("New Profile")
            .setView(editText)
            .setPositiveButton("Create") { _, _ ->
                val name = editText.text?.toString()?.trim()
                if (!name.isNullOrEmpty()) {
                    vm.createProfile(name)
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteProfile(profile: Profile) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Profile")
            .setMessage("Delete \"${profile.name}\"? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> vm.deleteProfile(profile) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
