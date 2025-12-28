package com.cortlandwalker.shortoftheweek.core.helpers

import com.cortlandwalker.shortoftheweek.data.models.Film

/**
 * Interface for Actions that involve toggling a bookmark.
 * Implement this in HomeAction, NewsAction, SearchAction, etc.
 */
interface BookmarkAction {
    val film: Film
}

/**
 * Helper extension to toggle the bookmark status of a specific film inside a list.
 * Returns a new list with the item updated (Optimistic UI update).
 */
fun List<Film>.updateBookmarkState(targetFilmId: Int): List<Film> {
    return map { film ->
        if (film.id == targetFilmId) {
            film.copy(isBookmarked = !film.isBookmarked)
        } else {
            film
        }
    }
}
