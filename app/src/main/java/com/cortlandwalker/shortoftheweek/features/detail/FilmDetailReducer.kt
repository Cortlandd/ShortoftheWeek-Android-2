package com.cortlandwalker.shortoftheweek.features.detail

import com.cortlandwalker.ghettoxide.Reducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FilmDetailReducer @Inject constructor(
    private val repo: FilmRepository
) : Reducer<FilmDetailState, FilmDetailAction, FilmDetailEffect>() {

    override fun onLoadAction(): FilmDetailAction = FilmDetailAction.OnLoad(
        filmId = currentState.filmId,
        film = currentState.film
    )

    override suspend fun process(action: FilmDetailAction) {
        when (action) {
            is FilmDetailAction.OnLoad -> {
                state { it.copy(filmId = action.filmId, film = action.film, viewDisplayMode = ViewDisplayMode.Loading) }
                loadFilm(forceRefresh = false, fromRefresh = false)
            }
            FilmDetailAction.OnRefresh -> loadFilm(forceRefresh = true, fromRefresh = true)

            is FilmDetailAction.Loaded -> {
                state { s ->
                    val mode = if (action.film != null) {
                        ViewDisplayMode.Content
                    } else {
                        ViewDisplayMode.Error("Not found")
                    }
                    s.copy(
                        film = action.film,
                        viewDisplayMode = mode,
                        isRefreshing = false
                    )
                }
            }
            is FilmDetailAction.Failed -> {
                state { s ->
                    val mode = if (s.film != null) s.viewDisplayMode else ViewDisplayMode.Error(action.message)
                    s.copy(viewDisplayMode = mode, isRefreshing = false)
                }
            }
            FilmDetailAction.OnPlayPressed -> {
                // Match iOS behavior: only load the embed when the user taps play.
                if (!currentState.film?.playUrl.isNullOrBlank()) {
                    state { it.copy(isPlaying = true) }
                }
            }
        }
    }

    private suspend fun loadFilm(forceRefresh: Boolean, fromRefresh: Boolean) {
        val filmId = currentState.filmId
        if (filmId <= 0 && currentState.film != null) {
            state { it.copy(viewDisplayMode = ViewDisplayMode.Content, film = currentState.film) }
            postAction(FilmDetailAction.Loaded(currentState.film, fromRefresh = fromRefresh))
            return
        }

        if (fromRefresh) {
            state { it.copy(isRefreshing = true) }
        } else {
            state { it.copy(viewDisplayMode = ViewDisplayMode.Loading) }
        }

        try {
            val film = withContext(Dispatchers.IO) {
                repo.getFilm(filmId) ?: run {
                    if (forceRefresh) {
                        repo.mixed(page = 1, limit = 20, forceRefresh = true)
                        repo.news(page = 1, limit = 20, forceRefresh = true)
                    }
                    repo.getFilm(filmId)
                }
            }
            postAction(FilmDetailAction.Loaded(film, fromRefresh = fromRefresh))
        } catch (t: Throwable) {
            postAction(FilmDetailAction.Failed(t.message ?: "Failed to load", fromRefresh = fromRefresh))
        }
    }
}
