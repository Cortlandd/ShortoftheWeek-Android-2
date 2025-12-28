package com.cortlandwalker.shortoftheweek.features.detail

import com.cortlandwalker.shortoftheweek.core.ViewModelReducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode.*
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FilmDetailReducer @Inject constructor(
    private val repo: FilmRepository
) : ViewModelReducer<FilmDetailState, FilmDetailAction, FilmDetailEffect>(FilmDetailState()) {

    override fun onLoadAction(): FilmDetailAction = FilmDetailAction.OnLoad(
        filmId = currentState.filmId,
        film = currentState.film
    )

    override suspend fun process(action: FilmDetailAction) {
        when (action) {
            is FilmDetailAction.OnLoad -> {
                load(action.filmId)
            }
            FilmDetailAction.OnRefresh -> {
                currentState.film?.id?.let { load(it, forceRefresh = true) }
            }

            is FilmDetailAction.Loaded -> {
                state { s ->
                    val mode = if (action.film != null) {
                        ViewDisplayMode.Content
                    } else {
                        Error("Not found")
                    }
                    s.copy(
                        film = action.film,
                        viewDisplayMode = mode,
                        isRefreshing = false
                    )
                }
            }
            is FilmDetailAction.SetInitialFilm -> {
                state {
                    it.copy(
                        film = action.film,
                        viewDisplayMode = ViewDisplayMode.Content
                    )
                }
            }
            is FilmDetailAction.Failed -> {
                state { s ->
                    val mode = if (s.film != null) s.viewDisplayMode else Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            FilmDetailAction.OnPlayPressed -> {
                if (!currentState.film?.playUrl.isNullOrBlank()) {
                    state { it.copy(isPlaying = true) }
                }
            }

            FilmDetailAction.OnBookmarkToggle -> {
                val film = currentState.film ?: return
                repo.toggleBookmark(film)
                state { it.copy(film = film.copy(isBookmarked = !film.isBookmarked)) }
            }
        }
    }

    private suspend fun load(filmId: Int, forceRefresh: Boolean = false) {
        // FIX: Don't show "Loading" spinner if we already have the film data visible.
        // We only show loading if we are starting from scratch.
        val hasData = currentState.film != null && currentState.viewDisplayMode == ViewDisplayMode.Content

        if (!hasData) {
            state { it.copy(viewDisplayMode = ViewDisplayMode.Loading) }
        } else {
            // If we have data, just ensure refreshing flag is set if needed,
            // but keep the user on Content view.
            state { it.copy(isRefreshing = true) }
        }

        try {
            // We still want to fetch the "full" film (with article HTML)
            // even if we displayed the cached search result.
            val film = withContext(Dispatchers.IO) {
                repo.getFilm(filmId)
            }

            if (film != null) {
                state {
                    it.copy(
                        film = film,
                        viewDisplayMode = ViewDisplayMode.Content,
                        isRefreshing = false
                    )
                }
            } else {
                // If DB miss and Repo returns null (maybe network failed?),
                // ONLY show error if we don't have the initial data shown.
                if (!hasData) {
                    state { it.copy(viewDisplayMode = ViewDisplayMode.Empty) }
                }
                // If we have data (from Search), we just silently fail the background refresh
                // effectively keeping the "Lite" version visible.
            }
        } catch (t: Throwable) {
            if (!hasData) {
                state { it.copy(viewDisplayMode = ViewDisplayMode.Error(t.message ?: "Error")) }
            }
        }
    }
}
