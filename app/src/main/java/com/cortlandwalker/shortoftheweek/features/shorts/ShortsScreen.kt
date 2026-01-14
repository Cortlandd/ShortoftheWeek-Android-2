package com.cortlandwalker.shortoftheweek.features.shorts

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
fun ShortsScreen(
    state: ShortsState,
    reducer: ShortsReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    ShortsScreenContent(
        state = state,
        onRefresh = { reducer.postAction(ShortsAction.OnRefresh) },
        onFilmClick = { reducer.postAction(ShortsAction.OnFilmSelected(it)) },
        onLoadMore = { reducer.postAction(ShortsAction.OnLoadMore) },
        onBookmarkToggle = { reducer.postAction(ShortsAction.OnBookmarkToggle(it)) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        sharedElementPrefix = sharedElementPrefix
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ShortsScreenContent(
    state: ShortsState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onLoadMore: () -> Unit,
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
        onLoadMore = onLoadMore,
        isLoadingPage = state.isLoadingPage,
        canLoadMore = state.canLoadMore,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        sharedElementPrefix = sharedElementPrefix
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun ShortsScreenPreview() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                ShortsScreenContent(
                    state = ShortsState(
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
                    onBookmarkToggle = {},
                    sharedElementPrefix = ""
                )
            }
        }
    }
}
