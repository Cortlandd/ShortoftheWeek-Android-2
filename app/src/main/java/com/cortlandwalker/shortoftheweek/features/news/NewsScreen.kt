package com.cortlandwalker.shortoftheweek.features.news

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.cortlandwalker.shortoftheweek.R
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.toSotwDisplayDateOrNull
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.CenterMessage
import com.cortlandwalker.shortoftheweek.ui.components.FilmCard
import com.cortlandwalker.shortoftheweek.ui.components.SotwCustomLoader
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsScreen(
    state: NewsState,
    reducer: NewsReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    NewsScreenContent(
        state = state,
        onRefresh = { reducer.postAction(NewsAction.OnRefresh) },
        onFilmClick = { reducer.postAction(NewsAction.OnFilmSelected(it)) },
        onLoadMore = { reducer.postAction(NewsAction.OnLoadMore) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun NewsScreenContent(
    state: NewsState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onLoadMore: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize()
        ) {
            when (val mode = state.viewDisplayMode) {
                ViewDisplayMode.Content -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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

                        // Footer: MORE button pagination
                        item(key = "news-footer") {
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
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp),
                                        contentAlignment = Alignment.Center
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

                ViewDisplayMode.Empty -> CenterMessage("No results")

                is ViewDisplayMode.Error -> CenterMessage(mode.message)

                ViewDisplayMode.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SotwCustomLoader()
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            NewsBannerHeader()
        }
    }
}

@Composable
private fun NewsBannerHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 2.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.news_banner),
            contentDescription = "News",
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 44.dp)
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Fit
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun NewsScreenPreview() {
    ShortOfTheWeekTheme {
        NewsScreenContent(
            state = NewsState(
                viewDisplayMode = ViewDisplayMode.Content,
                items = listOf(
                    Film(
                        id = 10,
                        kind = Film.Kind.NEWS,
                        title = "Festival Roundup",
                        slug = "festival-roundup",
                        synopsis = "Highlights from this week.",
                        postDate = "2024-09-05".toSotwDisplayDateOrNull(),
                        backgroundImageUrl = null,
                        thumbnailUrl = null,
                        filmmaker = null,
                        production = null,
                        durationMinutes = null,
                        playUrl = null,
                        textColorHex = "#FFFFFF",
                        articleHtml = "<p>Preview</p>"
                    )
                )
            ),
            onRefresh = {},
            onFilmClick = {},
            onLoadMore = {},
            animatedVisibilityScope = TODO(),
            sharedTransitionScope = TODO()
        )
    }
}
