package com.cortlandwalker.shortoftheweek.features.bookmarks

import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

data class BookmarksState(
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Loading,
    val items: List<Film> = emptyList(),
    val isRefreshing: Boolean = false
)

sealed interface BookmarksAction {
    data object OnLoad : BookmarksAction
    data object OnRefresh : BookmarksAction
    data class OnFilmSelected(val film: Film) : BookmarksAction
    data class OnBookmarkToggle(val film: Film) : BookmarksAction
}

sealed interface BookmarksEffect {
    data class OpenFilmDetail(val film: Film) : BookmarksEffect
}