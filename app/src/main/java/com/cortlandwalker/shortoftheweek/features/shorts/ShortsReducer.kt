package com.cortlandwalker.shortoftheweek.features.shorts

import android.util.Log
import com.cortlandwalker.shortoftheweek.core.ViewModelReducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode.*
import com.cortlandwalker.shortoftheweek.core.helpers.updateBookmarkState
import com.cortlandwalker.shortoftheweek.features.shorts.ShortsEffect.*
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ShortsReducer @Inject constructor(
    private val repo: FilmRepository
) : ViewModelReducer<ShortsState, ShortsAction, ShortsEffect>(ShortsState()) {

    init {
        postAction(onLoadAction())
    }

    override fun onLoadAction(): ShortsAction = ShortsAction.OnLoad

    override suspend fun process(action: ShortsAction) {
        when (action) {
            ShortsAction.OnLoad -> load(forceRefresh = false, fromRefresh = false)
            ShortsAction.OnRefresh -> load(forceRefresh = true, fromRefresh = true)
            ShortsAction.OnLoadMore -> loadMore()

            is ShortsAction.Loaded -> {
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
            is ShortsAction.Failed -> {
                state { s ->
                    val mode = if (s.items.isNotEmpty()) s.viewDisplayMode else Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            is ShortsAction.OnFilmSelected -> emit(OpenFilmDetail(action.film))
            is ShortsAction.OnBookmarkToggle -> {
                repo.toggleBookmark(action.film)
                state { s ->
                    s.copy(items = s.items.updateBookmarkState(action.film.id))
                }
            }
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
            postAction(ShortsAction.Loaded(updatedItems, fromRefresh = false))
        } catch (t: Throwable) {
            Log.e("ShortsReducer", t.message ?: "")
            postAction(ShortsAction.Failed(t.message ?: "Failed to load more", fromRefresh = false))
        }
    }

    private suspend fun load(forceRefresh: Boolean, fromRefresh: Boolean) {
        state { it.copy(isRefreshing = true, viewDisplayMode = ViewDisplayMode.Loading) }

        try {
            val items = withContext(Dispatchers.IO) {
                repo.mixed(page = 1, limit = 10, forceRefresh = forceRefresh)
            }
            postAction(ShortsAction.Loaded(items, fromRefresh = fromRefresh))
        } catch (t: Throwable) {
            Log.e("ShortsReducer", t.message ?: "")
            postAction(ShortsAction.Failed(t.message ?: "Failed to load", fromRefresh = fromRefresh))
        }
    }
}