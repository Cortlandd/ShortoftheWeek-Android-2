package com.cortlandwalker.shortoftheweek.features.home

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.CenterMessage
import com.cortlandwalker.shortoftheweek.ui.components.FilmCard
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
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreenContent(
    state: HomeState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onLoadMore: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        when (val mode = state.viewDisplayMode) {
            ViewDisplayMode.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(state.items, key = { it.id }) { film ->
                        FilmCard(
                            film = film,
                            // Ensure this key matches the one in FilmDetailScreen
                            sharedKey = "image-${film.id}",
                            onClick = { onFilmClick(film) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope
                        )
                    }

                    item(key = "home-footer") {

                        when {
                            state.isLoadingPage -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Button(
                                        onClick = {},
                                        enabled = false,
                                        colors = ButtonDefaults.buttonColors(
                                            disabledContentColor = Color.Black,
                                            disabledContainerColor = Color.White,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RectangleShape
                                    ) {
                                        Text("LOADING...")
                                    }
                                }
                            }

                            state.canLoadMore -> {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(44.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Button(
                                        onClick = onLoadMore,
                                        enabled = !state.isRefreshing,
                                        colors = ButtonDefaults.buttonColors(
                                            contentColor = Color.Black,
                                            containerColor = Color.White,
                                        ),
                                        modifier = Modifier.fillMaxSize(),
                                        shape = RectangleShape,
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("MORE")
                                    }
                                }
                            }
                            else -> {
                                Spacer(Modifier.height(18.dp))
                            }
                        }
                    }
                }
            }

            ViewDisplayMode.Empty -> {
                CenterMessage("No results")
            }

            is ViewDisplayMode.Error -> CenterMessage(mode.message)

            ViewDisplayMode.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun HomeScreenPreview() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
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
                animatedVisibilityScope = this as AnimatedVisibilityScope,
                sharedTransitionScope = this
            )
        }
    }
}
