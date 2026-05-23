package com.bedrocklaunch.ui.downloads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedrocklaunch.databinding.ItemDownloadBinding
import com.bedrocklaunch.model.DownloadItem
import com.bedrocklaunch.model.DownloadStatus

class DownloadAdapter(
    private val onCancel: (DownloadItem) -> Unit,
    private val onDelete: (DownloadItem) -> Unit
) : ListAdapter<DownloadItem, DownloadAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemDownloadBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: DownloadItem) {
            b.tvDownloadName.text = item.name
            b.tvDownloadStatus.text = item.status.name
            b.progressDownload.progress = item.progress
            b.tvProgress.text = "${item.progress}%"

            val isActive = item.status in listOf(DownloadStatus.PENDING, DownloadStatus.RUNNING)
            b.btnCancelDownload.visibility = if (isActive) android.view.View.VISIBLE else android.view.View.GONE
            b.btnDeleteDownload.visibility = if (!isActive) android.view.View.VISIBLE else android.view.View.GONE

            b.btnCancelDownload.setOnClickListener { onCancel(item) }
            b.btnDeleteDownload.setOnClickListener { onDelete(item) }

            if (item.errorMessage != null) {
                b.tvError.visibility = android.view.View.VISIBLE
                b.tvError.text = item.errorMessage
            } else {
                b.tvError.visibility = android.view.View.GONE
            }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(ItemDownloadBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<DownloadItem>() {
            override fun areItemsTheSame(a: DownloadItem, b: DownloadItem) = a.id == b.id
            override fun areContentsTheSame(a: DownloadItem, b: DownloadItem) = a == b
        }
    }
}
