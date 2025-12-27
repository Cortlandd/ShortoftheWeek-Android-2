package com.cortlandwalker.shortoftheweek.features.news

import android.util.Log
import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.shortoftheweek.core.ViewModelReducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.features.home.HomeAction
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class NewsReducer @Inject constructor(
    private val repo: FilmRepository
) : ViewModelReducer<NewsState, NewsAction, NewsEffect>(NewsState()) {

    init {
        postAction(onLoadAction())
    }

    override fun onLoadAction(): NewsAction = NewsAction.OnLoad

    override suspend fun process(action: NewsAction) {
        when (action) {
            NewsAction.OnLoad -> load(forceRefresh = false, fromRefresh = false)
            NewsAction.OnRefresh -> load(forceRefresh = true, fromRefresh = true)
            NewsAction.OnLoadMore -> loadMore()

            is NewsAction.Loaded -> {
                state { s ->
                    s.copy(
                        items = action.items,
                        viewDisplayMode = if (action.items.isEmpty()) ViewDisplayMode.Empty else ViewDisplayMode.Content,
                        isRefreshing = false,
                        isLoadingPage = false,
                        canLoadMore = action.items.isNotEmpty()
                    )
                }
            }
            is NewsAction.Failed -> {
                state { s ->
                    val mode = if (s.items.isNotEmpty()) s.viewDisplayMode else ViewDisplayMode.Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            is NewsAction.OnFilmSelected -> emit(NewsEffect.OpenFilmDetail(action.film))
        }
    }

    private suspend fun loadMore() {
        val newPage = currentState.page + 1
        state { it.copy(page = newPage, isLoadingPage = true) }

        try {
            val items = withContext(Dispatchers.IO) {
                repo.news(page = 1 + currentState.page, limit = 10, forceRefresh = false)
            }
            val updatedItems = currentState.items + items
            postAction(NewsAction.Loaded(updatedItems, fromRefresh = false))
        } catch (t: Throwable) {
            Log.e("HomeReducer", t.message ?: "")
            postAction(NewsAction.Failed(t.message ?: "Failed to load more", fromRefresh = false))
        }
    }

    private suspend fun load(forceRefresh: Boolean, fromRefresh: Boolean) {
        if (fromRefresh) {
            state { it.copy(isRefreshing = true) }
        } else {
            state { it.copy(viewDisplayMode = ViewDisplayMode.Loading) }
        }

        try {
            val items = withContext(Dispatchers.IO) {
                repo.news(page = currentState.page, limit = 10, forceRefresh = forceRefresh)
            }
            postAction(NewsAction.Loaded(items, fromRefresh = fromRefresh))
        } catch (t: Throwable) {
            postAction(NewsAction.Failed(t.message ?: "Failed to load", fromRefresh = fromRefresh))
        }
    }
}
