package com.cortlandwalker.shortoftheweek.features.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.CenterMessage
import com.cortlandwalker.shortoftheweek.ui.components.FilmCard
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreen(
    state: SearchState,
    reducer: SearchReducer,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    SearchScreenContent(
        state = state,
        onQueryChanged = { reducer.postAction(SearchAction.OnQueryChanged(it)) },
        onSubmit = { reducer.postAction(SearchAction.OnSubmit) },
        onRefresh = { reducer.postAction(SearchAction.OnRefresh) },
        onFilmClick = { reducer.postAction(SearchAction.OnFilmSelected(it)) },
        onRecentSearchClick = { reducer.postAction(SearchAction.OnRecentSearchClicked(it)) },
        onClearRecents = { reducer.postAction(SearchAction.OnClearRecentSearches) },
        onLoadMore = { reducer.postAction(SearchAction.OnLoadMore) },
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SearchScreenContent(
    state: SearchState,
    onQueryChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onRefresh: () -> Unit,
    onFilmClick: (Film) -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onClearRecents: () -> Unit,
    onLoadMore: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF212121))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChanged,
                    singleLine = true,
                    placeholder = { Text("Search...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedTextColor = Color.White
                    ),
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChanged("") }) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.width(10.dp))

                Button(
                    onClick = onSubmit,
                    enabled = state.query.trim().isNotEmpty() && !state.isRefreshing,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black,
                        disabledContentColor = Color.Black,
                        disabledContainerColor = Color.DarkGray
                    )
                ) {
                    Text("Go")
                }
            }

            when (val mode = state.viewDisplayMode) {
                ViewDisplayMode.Content -> {
                    if (!state.hasSearched) {
                        RecentSearchesSection(
                            recent = state.recentSearches,
                            onClick = onRecentSearchClick,
                            onClear = onClearRecents
                        )
                        CenterMessage("Search for films or news.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp)
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

                            item(key = "search-footer") {

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
                }
                ViewDisplayMode.Empty -> CenterMessage("No results")
                is ViewDisplayMode.Error -> CenterMessage(mode.message)
                ViewDisplayMode.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun RecentSearchesSection(
    recent: List<String>,
    onClick: (String) -> Unit,
    onClear: () -> Unit,
) {
    if (recent.isEmpty()) return

    Column(Modifier.padding(horizontal = 12.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Recent",
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                fontFamily = DomDiagonal,
                fontSize = 24.sp
            )
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onClear) {
                Text("Clear", color = Color.White)
            }
        }

        LazyRow(
            contentPadding = PaddingValues(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(recent, key = { it.lowercase() }) { q ->
                AssistChip(
                    onClick = { onClick(q) },
                    label = {
                        Text(
                            q,
                            color = Color.White
                        )
                    },
                    trailingIcon = {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Search recent",
                            tint = Color.White
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun SearchScreenPreviewEmpty() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            // FIX: Use AnimatedVisibility to provide the correct scope
            AnimatedVisibility(visible = true) {
                SearchScreenContent(
                    state = SearchState(
                        query = "",
                        hasSearched = false,
                        viewDisplayMode = ViewDisplayMode.Content,
                        recentSearches = listOf("no vacancy", "no vac"),
                        items = emptyList()
                    ),
                    onQueryChanged = {},
                    onSubmit = {},
                    onRefresh = {},
                    onFilmClick = {},
                    onRecentSearchClick = {},
                    onClearRecents = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = false)
@Composable
private fun SearchScreenPreview() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                SearchScreenContent(
                    state = SearchState(
                        query = "No Vacancy",
                        hasSearched = true,
                        viewDisplayMode = ViewDisplayMode.Content,
                        items = listOf(
                            Film(
                                id = 77,
                                kind = Film.Kind.VIDEO,
                                title = "Sample Film",
                                slug = "sample-film",
                                synopsis = "Preview synopsis",
                                postDate = "2024-09-03",
                                backgroundImageUrl = null,
                                thumbnailUrl = null,
                                filmmaker = "Bruh Man Walker",
                                production = null,
                                durationMinutes = 8,
                                playUrl = "https://www.shortoftheweek.com/",
                                textColorHex = "#FFFFFF",
                                articleHtml = "<p>Preview</p>"
                            )
                        )
                    ),
                    onQueryChanged = {},
                    onSubmit = {},
                    onRefresh = {},
                    onFilmClick = {},
                    onRecentSearchClick = {},
                    onClearRecents = {},
                    onLoadMore = {},
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout
                )
            }
        }
    }
}