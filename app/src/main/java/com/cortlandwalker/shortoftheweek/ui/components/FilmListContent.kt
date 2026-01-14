package com.cortlandwalker.shortoftheweek.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FilmListContent(
    items: List<Film>,
    viewDisplayMode: ViewDisplayMode,
    isRefreshing: Boolean,
    onRefresh: () -> Unit, onFilmClick: (Film) -> Unit,
    onBookmarkToggle: (Film) -> Unit, // Common callback
    onLoadMore: () -> Unit,
    isLoadingPage: Boolean,
    canLoadMore: Boolean,
    modifier: Modifier = Modifier,
    // Pass scopes for shared element transitions
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
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
                            sharedKey = "$sharedElementPrefix-image-${film.id}",
                            onClick = { onFilmClick(film) },
                            animatedVisibilityScope = animatedVisibilityScope,
                            sharedTransitionScope = sharedTransitionScope,
                            onBookmarkClick = { onBookmarkToggle(film) }
                        )
                    }

                    item(key = "footer") {
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
                        }
                    }
                }
            }
            ViewDisplayMode.Empty -> SotwEmptyState()
            is ViewDisplayMode.Error -> SotwErrorState(message = viewDisplayMode.message)
            ViewDisplayMode.Loading -> {

            }
        }
    }
}
