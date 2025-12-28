package com.cortlandwalker.shortoftheweek.features.news

import com.cortlandwalker.shortoftheweek.core.helpers.BookmarkAction
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.features.home.HomeAction

data class NewsState(
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Loading,
    val isRefreshing: Boolean = false,
    val items: List<Film> = emptyList(),
    val isLoadingPage: Boolean = false,
    val canLoadMore: Boolean = true,
    val page: Int = 1
)

sealed interface NewsAction {
    data object OnLoad : NewsAction
    data object OnRefresh : NewsAction
    data object OnLoadMore: NewsAction
    data class Loaded(val items: List<Film>, val fromRefresh: Boolean) : NewsAction
    data class Failed(val message: String, val fromRefresh: Boolean) : NewsAction
    data class OnFilmSelected(val film: Film) : NewsAction
    data class OnBookmarkToggle(override val film: Film) : NewsAction, BookmarkAction
}

sealed interface NewsEffect {
    data class OpenFilmDetail(val film: Film) : NewsEffect
}

