package com.bedrocklaunch.ui.news

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bedrocklaunch.databinding.FragmentNewsBinding
import com.bedrocklaunch.model.Result
import com.bedrocklaunch.viewmodel.NewsViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewsFragment : Fragment() {
    private var _b: FragmentNewsBinding? = null
    private val b get() = _b!!
    private val vm: NewsViewModel by viewModels()
    private lateinit var adapter: NewsAdapter

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentNewsBinding.inflate(i, c, false); return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = NewsAdapter { article ->
            if (article.url.isNotBlank()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(article.url)))
            }
        }
        b.rvNews.layoutManager = LinearLayoutManager(requireContext())
        b.rvNews.adapter = adapter

        b.swipeRefresh.setOnRefreshListener { vm.fetchNews() }

        vm.news.observe(viewLifecycleOwner) { result ->
            b.swipeRefresh.isRefreshing = result is Result.Loading
            when (result) {
                is Result.Loading -> {}
                is Result.Success -> {
                    adapter.submitList(result.data)
                    b.tvEmpty.visibility = if (result.data.isEmpty()) View.VISIBLE else View.GONE
                }
                is Result.Error -> {
                    Snackbar.make(b.root, "Failed to load news: ${result.message}", Snackbar.LENGTH_LONG)
                        .setAction("Retry") { vm.fetchNews() }.show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _b = null }
}
