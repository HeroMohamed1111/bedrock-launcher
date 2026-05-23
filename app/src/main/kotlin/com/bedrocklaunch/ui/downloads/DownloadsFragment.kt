package com.bedrocklaunch.ui.downloads

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bedrocklaunch.databinding.FragmentDownloadsBinding
import com.bedrocklaunch.viewmodel.DownloadsViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DownloadsFragment : Fragment() {
    private var _b: FragmentDownloadsBinding? = null
    private val b get() = _b!!
    private val vm: DownloadsViewModel by viewModels()
    private lateinit var adapter: DownloadAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentDownloadsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = DownloadAdapter(
            onCancel = { item -> vm.cancelDownload(item.id) },
            onDelete = { item -> vm.deleteDownload(item.id) }
        )
        b.rvDownloads.layoutManager = LinearLayoutManager(requireContext())
        b.rvDownloads.adapter = adapter

        b.btnClearFinished.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Clear Finished")
                .setMessage("Remove all completed, failed, and cancelled downloads?")
                .setPositiveButton("Clear") { _, _ ->
                    vm.clearFinished()
                    Snackbar.make(b.root, "Cleared", Snackbar.LENGTH_SHORT).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        vm.allDownloads.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            b.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            b.tvCount.text = "${list.size} item(s)"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
