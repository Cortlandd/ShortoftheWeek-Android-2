package com.cortlandwalker.shortoftheweek.features.home

import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

data class HomeState(
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Loading,
    val isRefreshing: Boolean = false,
    val items: List<Film> = emptyList(),
    val isLoadingPage: Boolean = false,
    val canLoadMore: Boolean = true,
    val page: Int = 1
)

sealed interface HomeAction {
    data object OnLoad : HomeAction
    data object OnRefresh : HomeAction
    data object OnLoadMore: HomeAction
    data class Loaded(val items: List<Film>, val fromRefresh: Boolean) : HomeAction
    data class Failed(val message: String, val fromRefresh: Boolean) : HomeAction
    data class OnFilmSelected(val film: Film) : HomeAction
}

sealed interface HomeEffect {
    data class OpenFilmDetail(val film: Film) : HomeEffect
}