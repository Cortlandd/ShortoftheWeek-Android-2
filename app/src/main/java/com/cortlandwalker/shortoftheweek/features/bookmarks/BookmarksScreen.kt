package com.cortlandwalker.shortoftheweek.features.bookmarks

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.FilmListContent


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookmarksScreen(
    state: BookmarksState,
    reducer: BookmarksReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    BookmarksScreenContent(
        state = state,
        onRefresh = { reducer.postAction(BookmarksAction.OnRefresh) },
        onFilmClick = { reducer.postAction(BookmarksAction.OnFilmSelected(it)) },
        onBookmarkToggle = { reducer.postAction(BookmarksAction.OnBookmarkToggle(it)) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        sharedElementPrefix = sharedElementPrefix
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun BookmarksScreenContent(
    state: BookmarksState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onBookmarkToggle: (Film) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    FilmListContent(
        items = state.items,
        viewDisplayMode = state.viewDisplayMode,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        onFilmClick = onFilmClick,
        onBookmarkToggle = onBookmarkToggle,
        onLoadMore = {},
        isLoadingPage = false,
        canLoadMore = false,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        sharedElementPrefix = sharedElementPrefix
    )
}