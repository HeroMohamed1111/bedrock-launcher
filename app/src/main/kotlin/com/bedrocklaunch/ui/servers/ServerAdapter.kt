package com.bedrocklaunch.ui.servers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bedrocklaunch.databinding.ItemServerBinding
import com.bedrocklaunch.model.ServerInfo

class ServerAdapter(
    private val onJoin: (ServerInfo) -> Unit,
    private val onFavorite: (ServerInfo, Boolean) -> Unit,
    private val onPing: (ServerInfo) -> Unit
) : ListAdapter<ServerInfo, ServerAdapter.VH>(DIFF) {

    private val pings = mutableMapOf<String, Long>()

    fun updatePings(newPings: Map<String, Long>) {
        pings.putAll(newPings)
        notifyDataSetChanged()
    }

    inner class VH(private val b: ItemServerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(server: ServerInfo) {
            b.tvServerName.text = server.name
            b.tvServerHost.text = "${server.host}:${server.port}"
            b.tvServerMotd.text = server.motd.ifBlank { "No description" }
            b.tvPlayerCount.text = if (server.maxPlayers > 0)
                "${server.playerCount}/${server.maxPlayers}" else "?"
            b.tvVersion.text = server.version.ifBlank { "Bedrock" }

            val ping = pings[server.id] ?: server.ping
            b.tvPing.text = if (ping < 0) "—" else "${ping}ms"
            b.tvPing.setTextColor(
                b.root.context.getColor(
                    when {
                        ping < 0 -> android.R.color.darker_gray
                        ping < 100 -> android.R.color.holo_green_dark
                        ping < 250 -> android.R.color.holo_orange_dark
                        else -> android.R.color.holo_red_dark
                    }
                )
            )
            b.ivVerified.visibility =
                if (server.isVerified) android.view.View.VISIBLE else android.view.View.GONE

            // ToggleButton — suppress listener while setting programmatic state
            b.btnFavorite.setOnCheckedChangeListener(null)
            b.btnFavorite.isChecked = server.isFavorite
            b.btnFavorite.setOnCheckedChangeListener { _, checked -> onFavorite(server, checked) }

            b.btnJoin.setOnClickListener { onJoin(server) }
            b.btnPing.setOnClickListener { onPing(server) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(ItemServerBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ServerInfo>() {
            override fun areItemsTheSame(a: ServerInfo, b: ServerInfo) = a.id == b.id
            override fun areContentsTheSame(a: ServerInfo, b: ServerInfo) = a == b
        }
    }
}
