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
        state { it.copy(isRefreshing = true) }

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
                // If network fails but we have cached data, just stop refreshing
                if (currentState.film == null) {
                    state { it.copy(viewDisplayMode = ViewDisplayMode.Empty) }
                } else {
                    state { it.copy(isRefreshing = false) }
                }
            }
        } catch (t: Throwable) {
            if (currentState.film == null) {
                state { it.copy(viewDisplayMode = ViewDisplayMode.Error(t.message ?: "Error")) }
            } else {
                state { it.copy(isRefreshing = false) }
            }
        }
    }
}
