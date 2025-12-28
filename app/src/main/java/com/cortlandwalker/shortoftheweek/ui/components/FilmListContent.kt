package com.cortlandwalker.shortoftheweek.ui.components

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
import com.cortlandwalker.shortoftheweek.ui.components.SotwEmptyState
import com.cortlandwalker.shortoftheweek.ui.components.SotwErrorState
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme


@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilmListContent(
    items: List<Film>,
    viewDisplayMode: ViewDisplayMode,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,    onFilmClick: (Film) -> Unit,
    onBookmarkToggle: (Film) -> Unit, // Common callback
    onLoadMore: () -> Unit,
    isLoadingPage: Boolean,
    canLoadMore: Boolean,
    modifier: Modifier = Modifier,
    // Pass scopes for shared element transitions
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize()
    ) {
        when (viewDisplayMode) {
            ViewDisplayMode.Content -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(items, key = { it.id }) { film ->
                        FilmCard(
                            film = film,
                            sharedKey = "image-${film.id}",
                            onClick = { onFilmClick(film) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            // Logic is handled by the parent passing the specific Action
                            onBookmarkClick = { onBookmarkToggle(film) }
                        )
                    }

                    item(key = "home-footer") {

                        when {
                            isLoadingPage -> {
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

                            canLoadMore -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Button(
                                        onClick = onLoadMore,
                                        enabled = !isRefreshing,
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
            ViewDisplayMode.Empty -> SotwEmptyState()
            is ViewDisplayMode.Error -> SotwErrorState(message = viewDisplayMode.message)
            ViewDisplayMode.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    SotwCustomLoader()
                }
            }
        }
    }
}
