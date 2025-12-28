package com.cortlandwalker.shortoftheweek.features.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.FilmListContent
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    state: HomeState,
    reducer: HomeReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    HomeScreenContent(
        state = state,
        onRefresh = { reducer.postAction(HomeAction.OnRefresh) },
        onFilmClick = { reducer.postAction(HomeAction.OnFilmSelected(it)) },
        onLoadMore = { reducer.postAction(HomeAction.OnLoadMore) },
        onBookmarkToggle = { reducer.postAction(HomeAction.OnBookmarkToggle(it)) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onLoadMore: () -> Unit,
    onBookmarkToggle: (Film) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    FilmListContent(
        items = state.items,
        viewDisplayMode = state.viewDisplayMode,
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        onFilmClick = onFilmClick,
        onBookmarkToggle = onBookmarkToggle,
        onLoadMore = onLoadMore,
        isLoadingPage = state.isLoadingPage,
        canLoadMore = state.canLoadMore,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun HomeScreenPreview() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreenContent(
                    state = HomeState(
                        viewDisplayMode = ViewDisplayMode.Content,
                        items = listOf(
                            Film(
                                id = 1,
                                kind = Film.Kind.VIDEO,
                                title = "No Vacancy",
                                slug = "no-vacancy",
                                synopsis = "A restless, beautifully disorienting swirl.",
                                postDate = "2024-09-01",
                                backgroundImageUrl = "https://www.shortoftheweek.com/wp-content/uploads/2024/09/NoVacancy_Thumb.jpg",
                                thumbnailUrl = null,
                                filmmaker = "Miguel Rodrick",
                                production = "Short of the Week",
                                durationMinutes = 12,
                                playUrl = "https://www.shortoftheweek.com/2024/09/no-vacancy/",
                                textColorHex = "#FFFFFF",
                                articleHtml = "<p>Preview</p>"
                            ),
                            Film(
                                id = 2,
                                kind = Film.Kind.VIDEO,
                                title = "No Vacancy",
                                slug = "no-vacancy",
                                synopsis = "A restless, beautifully disorienting swirl.",
                                postDate = "2024-09-01",
                                backgroundImageUrl = "https://www.shortoftheweek.com/wp-content/uploads/2024/09/NoVacancy_Thumb.jpg",
                                thumbnailUrl = null,
                                filmmaker = "Miguel Rodrick",
                                production = "Short of the Week",
                                durationMinutes = 12,
                                playUrl = "https://www.shortoftheweek.com/2024/09/no-vacancy/",
                                textColorHex = "#FFFFFF",
                                articleHtml = "<p>Preview</p>"
                            )
                        )
                    ),
                    onRefresh = {},
                    onFilmClick = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onBookmarkToggle = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun HomeScreenPreviewEmpty() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreenContent(
                    state = HomeState(
                        viewDisplayMode = ViewDisplayMode.Empty,
                        items = listOf()
                    ),
                    onRefresh = {},
                    onFilmClick = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onBookmarkToggle = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun HomeScreenPreviewError() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreenContent(
                    state = HomeState(
                        viewDisplayMode = ViewDisplayMode.Error(message = "Network Error Occurred"),
                        items = listOf()
                    ),
                    onRefresh = {},
                    onFilmClick = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onBookmarkToggle = {}
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun HomeScreenPreviewLoading() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                HomeScreenContent(
                    state = HomeState(
                        viewDisplayMode = ViewDisplayMode.Loading,
                        items = listOf()
                    ),
                    onRefresh = {},
                    onFilmClick = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    onBookmarkToggle = {}
                )
            }
        }
    }
}
