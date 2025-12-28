package com.cortlandwalker.shortoftheweek.features.detail

import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

data class FilmDetailState(
    val filmId: Int = -1,
    val film: Film? = null,
    val isPlaying: Boolean = false,
    val viewDisplayMode: ViewDisplayMode = ViewDisplayMode.Content,
    val isRefreshing: Boolean = false
)

sealed interface FilmDetailAction {
    data class OnLoad(val filmId: Int, val film: Film? = null) : FilmDetailAction
    data object OnRefresh : FilmDetailAction
    data class Loaded(val film: Film?, val fromRefresh: Boolean) : FilmDetailAction
    data class SetInitialFilm(val film: Film) : FilmDetailAction
    data class Failed(val message: String, val fromRefresh: Boolean) : FilmDetailAction
    data object OnBookmarkToggle : FilmDetailAction
    data object OnPlayPressed : FilmDetailAction
}

sealed interface FilmDetailEffect

