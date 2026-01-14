package com.cortlandwalker.shortoftheweek.features.shorts

import com.cortlandwalker.shortoftheweek.core.helpers.BookmarkAction
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

data class ShortsState(
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Loading,
    val isRefreshing: Boolean = false,
    val items: List<Film> = emptyList(),
    val isLoadingPage: Boolean = false,
    val canLoadMore: Boolean = true,
    val page: Int = 1
)

sealed interface ShortsAction {
    data object OnLoad : ShortsAction
    data object OnRefresh : ShortsAction
    data object OnLoadMore: ShortsAction
    data class Loaded(val items: List<Film>, val fromRefresh: Boolean) : ShortsAction
    data class OnBookmarkToggle(override val film: Film) : ShortsAction, BookmarkAction
    data class Failed(val message: String, val fromRefresh: Boolean) : ShortsAction
    data class OnFilmSelected(val film: Film) : ShortsAction
}

sealed interface ShortsEffect {
    data class OpenFilmDetail(val film: Film) : ShortsEffect
}