package com.bedrocklaunch.ui.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bedrocklaunch.R
import com.bedrocklaunch.databinding.ItemNewsBinding
import com.bedrocklaunch.model.NewsArticle
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(
    private val onClick: (NewsArticle) -> Unit
) : ListAdapter<NewsArticle, NewsAdapter.VH>(DIFF) {

    inner class VH(private val b: ItemNewsBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(article: NewsArticle) {
            b.tvNewsTitle.text = article.title
            b.tvNewsSummary.text = article.summary
            b.tvNewsSource.text = article.source
            b.tvNewsDate.text = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(Date(article.publishedAt))

            Glide.with(b.imgNewsThumb)
                .load(article.imageUrl)
                .placeholder(R.drawable.ic_news_placeholder)
                .error(R.drawable.ic_news_placeholder)
                .centerCrop()
                .into(b.imgNewsThumb)

            b.root.setOnClickListener { onClick(article) }
        }
    }

    override fun onCreateViewHolder(p: ViewGroup, t: Int): VH =
        VH(ItemNewsBinding.inflate(LayoutInflater.from(p.context), p, false))

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(getItem(pos))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<NewsArticle>() {
            override fun areItemsTheSame(a: NewsArticle, b: NewsArticle) = a.id == b.id
            override fun areContentsTheSame(a: NewsArticle, b: NewsArticle) = a == b
        }
    }
}
