package com.bedrocklaunch.viewmodel

import androidx.lifecycle.*
import com.bedrocklaunch.model.NewsArticle
import com.bedrocklaunch.model.Result
import com.bedrocklaunch.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewsViewModel @Inject constructor(
    private val repo: NewsRepository
) : ViewModel() {

    private val _news = MutableLiveData<Result<List<NewsArticle>>>(Result.Loading)
    val news: LiveData<Result<List<NewsArticle>>> = _news

    init { fetchNews() }

    fun fetchNews() {
        _news.value = Result.Loading
        viewModelScope.launch {
            _news.value = repo.fetchNews()
        }
    }
}
