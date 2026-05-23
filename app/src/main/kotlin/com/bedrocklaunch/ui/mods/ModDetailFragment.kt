package com.bedrocklaunch.ui.mods

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.FragmentModDetailBinding
import com.bedrocklaunch.model.DownloadStatus
import com.bedrocklaunch.repository.DownloadRepository
import com.bedrocklaunch.viewmodel.ModsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class ModDetailFragment : Fragment() {

    private var _b: FragmentModDetailBinding? = null
    private val b get() = _b!!
    private val vm: ModsViewModel by viewModels({ requireParentFragment().requireParentFragment() })

    @Inject lateinit var downloadRepo: DownloadRepository

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentModDetailBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.selectedMod.observe(viewLifecycleOwner) { mod ->
            if (mod == null) return@observe
            b.tvDetailName.text = mod.name
            b.tvDetailAuthor.text = "by ${mod.author}"
            b.tvDetailSummary.text = mod.summary
            b.tvDetailDescription.text = mod.description.ifBlank { mod.summary }
            b.tvDetailVersion.text = "Latest: ${mod.latestVersion}"
            b.tvDetailDownloads.text = "${mod.downloadCount} downloads"
            b.tvDetailSource.text = mod.source.name
            b.chipGameVersions.text = mod.gameVersions.take(3).joinToString(", ")

            Glide.with(b.imgDetailIcon)
                .load(mod.iconUrl)
                .placeholder(R.drawable.ic_mod_placeholder)
                .circleCrop()
                .into(b.imgDetailIcon)

            b.btnDownloadMod.setOnClickListener {
                if (mod.downloadUrl.isBlank()) {
                    Snackbar.make(b.root, "No direct download available", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                CoroutineScope(Dispatchers.Main).launch {
                    val fileName = mod.downloadUrl.substringAfterLast("/")
                    val destPath = "${requireContext().getExternalFilesDir(null)}/downloads/$fileName"
                    downloadRepo.enqueueDownload(mod.name, mod.downloadUrl, destPath)
                    Snackbar.make(b.root, "Download queued: ${mod.name}", Snackbar.LENGTH_SHORT).show()
                }
            }

            b.btnOpenWeb.setOnClickListener {
                if (mod.projectUrl.isNotBlank()) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(mod.projectUrl)))
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
