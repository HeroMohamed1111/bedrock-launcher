package com.bedrocklaunch.ui.launcher

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedrocklaunch.databinding.ItemProfileBinding
import com.bedrocklaunch.model.Profile

class ProfileAdapter(
    private val onSelect: (Profile) -> Unit,
    private val onDelete: (Profile) -> Unit
) : ListAdapter<Profile, ProfileAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemProfileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(profile: Profile) {
            b.tvProfileName.text = profile.name
            b.tvProfileNotes.text = profile.notes.ifBlank { "No notes" }
            b.cardProfile.strokeWidth = if (profile.isActive) 4 else 0
            b.root.setOnClickListener { onSelect(profile) }
            b.btnDeleteProfile.setOnClickListener { onDelete(profile) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(ItemProfileBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Profile>() {
            override fun areItemsTheSame(a: Profile, b: Profile) = a.id == b.id
            override fun areContentsTheSame(a: Profile, b: Profile) = a == b
        }
    }
}
