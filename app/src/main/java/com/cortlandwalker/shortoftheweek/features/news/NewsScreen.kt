package com.cortlandwalker.shortoftheweek.features.news


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.cortlandwalker.shortoftheweek.R
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.toSotwDisplayDateOrNull
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.FilmListContent
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsScreen(
    state: NewsState,
    reducer: NewsReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    NewsScreenContent(
        state = state,
        onRefresh = { reducer.postAction(NewsAction.OnRefresh) },
        onFilmClick = { reducer.postAction(NewsAction.OnFilmSelected(it)) },
        onLoadMore = { reducer.postAction(NewsAction.OnLoadMore) },
        onBookmarkToggle = { reducer.postAction(NewsAction.OnBookmarkToggle(it)) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        sharedElementPrefix = sharedElementPrefix
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewsScreenContent(
    state: NewsState,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onLoadMore: () -> Unit,
    onBookmarkToggle: (Film) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Shared List Component
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

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .zIndex(1f)
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
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
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
                    onBookmarkToggle = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    sharedElementPrefix = ""
                )
            }
        }
    }
}
