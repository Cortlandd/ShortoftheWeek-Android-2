package com.cortlandwalker.shortoftheweek.features.search

import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

data class SearchState(
    val query: String = "",
    val hasSearched: Boolean = false,
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Content,
    val isRefreshing: Boolean = false,
    val items: List<Film> = emptyList(),
    val isLoadingPage: Boolean = false,
    val canLoadMore: Boolean = true,
    val page: Int = 1,

    val recentSearches: List<String> = emptyList()
)

sealed interface SearchAction {
    data object OnLoad : SearchAction
    data object OnLoadMore: SearchAction
    data class OnQueryChanged(val query: String) : SearchAction
    data object OnSubmit : SearchAction
    data object OnRefresh : SearchAction
    data class Loaded(val items: List<Film>) : SearchAction
    data class Failed(val message: String) : SearchAction
    data class OnFilmSelected(val film: Film) : SearchAction

    data class OnRecentSearchesLoaded(val items: List<String>) : SearchAction
    data class OnRecentSearchClicked(val query: String) : SearchAction
    data object OnClearRecentSearches : SearchAction
}

sealed interface SearchEffect {
    data class OpenFilmDetail(val film: Film) : SearchEffect
}

