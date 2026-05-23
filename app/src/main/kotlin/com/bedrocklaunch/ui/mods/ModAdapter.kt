package com.bedrocklaunch.ui.mods

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.ItemModBinding
import com.bedrocklaunch.model.Mod

class ModAdapter(
    private val onModClick: (Mod) -> Unit,
    private val onBookmark: (Mod) -> Unit
) : ListAdapter<Mod, ModAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemModBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(mod: Mod) {
            b.tvModName.text = mod.name
            b.tvModSummary.text = mod.summary
            b.tvModAuthor.text = "by ${mod.author}"
            b.tvDownloads.text = formatCount(mod.downloadCount)
            b.tvVersion.text = mod.latestVersion.ifBlank { "?" }
            b.chipSource.text = mod.source.name

            Glide.with(b.imgModIcon)
                .load(mod.iconUrl)
                .placeholder(R.drawable.ic_mod_placeholder)
                .error(R.drawable.ic_mod_placeholder)
                .circleCrop()
                .into(b.imgModIcon)

            b.root.setOnClickListener { onModClick(mod) }
            b.btnBookmark.setOnClickListener { onBookmark(mod) }
        }

        private fun formatCount(count: Long): String = when {
            count >= 1_000_000 -> "${count / 1_000_000}M"
            count >= 1_000 -> "${count / 1_000}K"
            else -> count.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemModBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Mod>() {
            override fun areItemsTheSame(a: Mod, b: Mod) = a.id == b.id
            override fun areContentsTheSame(a: Mod, b: Mod) = a == b
        }
    }
}
