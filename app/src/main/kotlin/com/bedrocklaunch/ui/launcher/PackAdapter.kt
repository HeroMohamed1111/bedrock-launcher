package com.bedrocklaunch.ui.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedrocklaunch.databinding.ItemPackBinding
import com.bedrocklaunch.model.Pack

class PackAdapter(
    private val onToggle: (Pack, Boolean) -> Unit
) : ListAdapter<Pack, PackAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemPackBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(pack: Pack) {
            b.tvPackName.text = pack.name
            b.tvPackType.text = pack.type.name
            b.tvPackVersion.text = pack.version.ifBlank { "?" }
            b.switchPack.isChecked = pack.isEnabled
            b.switchPack.setOnCheckedChangeListener { _, checked -> onToggle(pack, checked) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemPackBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Pack>() {
            override fun areItemsTheSame(a: Pack, b: Pack) = a.uuid == b.uuid
            override fun areContentsTheSame(a: Pack, b: Pack) = a == b
        }
    }
}
