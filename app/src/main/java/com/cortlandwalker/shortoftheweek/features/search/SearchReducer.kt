package com.cortlandwalker.shortoftheweek.features.search

import android.util.Log
import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode.*
import com.cortlandwalker.shortoftheweek.data.recent.RecentSearchesStore
import com.cortlandwalker.shortoftheweek.features.search.SearchEffect.*
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SearchReducer @Inject constructor(
    private val repo: FilmRepository,
    private val recentStore: RecentSearchesStore
) : Reducer<SearchState, SearchAction, SearchEffect>() {

    override fun onLoadAction(): SearchAction = SearchAction.OnLoad

    override suspend fun process(action: SearchAction) {
        when (action) {
            SearchAction.OnLoad -> {
                scope.launch {
                    recentStore.recentSearchesFlow().collect { recentSearches ->
                        postAction(SearchAction.OnRecentSearchesLoaded(recentSearches))
                    }
                }
            }
            SearchAction.OnLoadMore -> loadMore()
            is SearchAction.OnQueryChanged -> state { it.copy(query = action.query) }
            SearchAction.OnSubmit -> performSearch()
            SearchAction.OnRefresh -> performSearch()

            is SearchAction.Loaded -> {
                state { s ->
                    s.copy(
                        hasSearched = true,
                        items = action.items,
                        viewDisplayMode = if (action.items.isEmpty()) ViewDisplayMode.Empty else ViewDisplayMode.Content,
                        isRefreshing = false,
                        isLoadingPage = false,
                        canLoadMore = action.items.isNotEmpty()
                    )
                }
                recentStore.add(currentState.query)
            }
            is SearchAction.Failed -> {
                state { s ->
                    val mode = if (s.items.isNotEmpty()) s.viewDisplayMode else Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            is SearchAction.OnFilmSelected -> emit(OpenFilmDetail(film = action.film))

            // Handle Recent Searches
            SearchAction.OnClearRecentSearches -> {
                recentStore.clear()
            }
            is SearchAction.OnRecentSearchClicked -> {
                postAction(SearchAction.OnQueryChanged(action.query))
            }
            is SearchAction.OnRecentSearchesLoaded -> {
                state {
                    it.copy(recentSearches = action.items)
                }
            }
        }
    }

    private suspend fun loadMore() {
        val newPage = currentState.page + 1
        state {
            it.copy(isLoadingPage = true, page = newPage)
        }

        val q = currentState.query.trim()
        try {
            val items = withContext(Dispatchers.IO) {
                repo.search(q = q, page = newPage, limit = 10)
            }
            val updatedItems = currentState.items + items
            postAction(SearchAction.Loaded(updatedItems))
        } catch (t: Throwable) {
            postAction(SearchAction.Failed(t.message ?: "Failed to search"))
        }
    }

    private suspend fun performSearch() {
        val q = currentState.query.trim()
        if (q.isBlank()) {
            state { it.copy(hasSearched = false, items = emptyList(), viewDisplayMode = ViewDisplayMode.Content, isRefreshing = false) }
            return
        }

        state { it.copy(viewDisplayMode = ViewDisplayMode.Loading) }

        try {
            val items = withContext(Dispatchers.IO) {
                Log.d("SearchReducer", "performSearch(q=$q)")
                repo.search(q = q, page = 1, limit = 10)
            }
            postAction(SearchAction.Loaded(items))
        } catch (t: Throwable) {
            Log.e("SearchReducer", "performSearch", t)
            postAction(SearchAction.Failed(t.message ?: "Failed to search"))
        }
    }
}
