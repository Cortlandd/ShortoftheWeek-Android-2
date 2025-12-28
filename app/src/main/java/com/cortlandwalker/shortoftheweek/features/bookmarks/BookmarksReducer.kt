package com.cortlandwalker.shortoftheweek.features.bookmarks

import androidx.activity.result.launch
import androidx.compose.ui.geometry.isEmpty
import com.cortlandwalker.shortoftheweek.core.ViewModelReducer
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.networking.repository.FilmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BookmarksReducer @Inject constructor(
    private val repo: FilmRepository
) : ViewModelReducer<BookmarksState, BookmarksAction, BookmarksEffect>(BookmarksState()) {

    init {
        postAction(onLoadAction())
    }

    override fun onLoadAction() = BookmarksAction.OnLoad

    override suspend fun process(action: BookmarksAction) {
        when (action) {
            BookmarksAction.OnLoad -> {
                scope.launch {
                    state { it.copy(viewDisplayMode = ViewDisplayMode.Loading) }

                    repo.getBookmarkedFilmsFlow()
                        .catch { e ->
                            state { it.copy(viewDisplayMode = ViewDisplayMode.Error(e.message ?: "Unknown error")) }
                        }
                        .collectLatest { items ->
                            state {
                                it.copy(
                                    items = items,
                                    viewDisplayMode = if (items.isEmpty()) ViewDisplayMode.Empty else ViewDisplayMode.Content,
                                    isRefreshing = false
                                )
                            }
                        }
                }
            }
            BookmarksAction.OnRefresh -> state { it.copy(isRefreshing = false) }
            is BookmarksAction.OnFilmSelected -> emit(BookmarksEffect.OpenFilmDetail(action.film))
            is BookmarksAction.OnBookmarkToggle -> {
                repo.toggleBookmark(action.film)
            }
        }
    }
}