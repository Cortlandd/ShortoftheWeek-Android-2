package com.cortlandwalker.shortoftheweek.features.home

import android.util.Log
import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class HomeReducer @Inject constructor(
    private val repo: FilmRepository
) : Reducer<HomeState, HomeAction, HomeEffect>() {

    override fun onLoadAction(): HomeAction = HomeAction.OnLoad

    override suspend fun process(action: HomeAction) {
        when (action) {
            HomeAction.OnLoad -> load(forceRefresh = false, fromRefresh = false)
            HomeAction.OnRefresh -> load(forceRefresh = true, fromRefresh = true)
            HomeAction.OnLoadMore -> loadMore()

            is HomeAction.Loaded -> {
                state {
                    it.copy(
                        items = action.items,
                        viewDisplayMode = if (action.items.isEmpty()) ViewDisplayMode.Empty else ViewDisplayMode.Content,
                        isRefreshing = false,
                        isLoadingPage = false,
                        canLoadMore = action.items.isNotEmpty()
                    )
                }
            }
            is HomeAction.Failed -> {
                state { s ->
                    val mode = if (s.items.isNotEmpty()) s.viewDisplayMode else ViewDisplayMode.Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            is HomeAction.OnFilmSelected -> emit(HomeEffect.OpenFilmDetail(action.film.id))
        }
    }

    private suspend fun loadMore() {
        val newPage = currentState.page + 1
        state { it.copy(page = newPage, isLoadingPage = true) }

        try {
            val items = withContext(Dispatchers.IO) {
                repo.mixed(page = 1 + currentState.page, limit = 10, forceRefresh = false)
            }
            val updatedItems = currentState.items + items
            postAction(HomeAction.Loaded(updatedItems, fromRefresh = false))
        } catch (t: Throwable) {
            Log.e("HomeReducer", t.message ?: "")
            postAction(HomeAction.Failed(t.message ?: "Failed to load more", fromRefresh = false))
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
                repo.mixed(page = 1, limit = 10, forceRefresh = forceRefresh)
            }
            postAction(HomeAction.Loaded(items, fromRefresh = fromRefresh))
        } catch (t: Throwable) {
            Log.e("HomeReducer", t.message ?: "")
            postAction(HomeAction.Failed(t.message ?: "Failed to load", fromRefresh = fromRefresh))
        }
    }
}
