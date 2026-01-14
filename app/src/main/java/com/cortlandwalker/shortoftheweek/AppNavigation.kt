package com.cortlandwalker.shortoftheweek

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cortlandwalker.shortoftheweek.core.helpers.fromHex
import com.cortlandwalker.shortoftheweek.core.navigation.Routes
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksEffect
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksReducer
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksScreen
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailAction
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailReducer
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailScreen
import com.cortlandwalker.shortoftheweek.features.news.NewsEffect
import com.cortlandwalker.shortoftheweek.features.news.NewsReducer
import com.cortlandwalker.shortoftheweek.features.news.NewsScreen
import com.cortlandwalker.shortoftheweek.features.search.SearchEffect
import com.cortlandwalker.shortoftheweek.features.search.SearchReducer
import com.cortlandwalker.shortoftheweek.features.search.SearchScreen
import com.cortlandwalker.shortoftheweek.features.shorts.ShortsEffect
import com.cortlandwalker.shortoftheweek.features.shorts.ShortsReducer
import com.cortlandwalker.shortoftheweek.features.shorts.ShortsScreen
import com.google.gson.Gson

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    sharedTransitionScope: SharedTransitionScope
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute != Routes.Detail

    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar(
                    containerColor = Color(0xFF1A1A1A),
                    modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                ) {
                    val items = listOf(
                        Triple(Routes.Home, "SHORTS", Icons.Default.Movie),
                        Triple(Routes.News, "NEWS", Icons.Default.Newspaper),
                        Triple(Routes.Search, "SEARCH", Icons.Default.Search),
                        Triple(Routes.Bookmarks, "SAVED", Icons.Default.Favorite)
                    )

                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            selected = currentRoute == route,
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(text = label, fontSize = 12.sp)
                                    if (currentRoute == route) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Canvas(modifier = Modifier.size(4.dp)) {
                                            drawCircle(color = Color.fromHex("#647370"))
                                        }
                                    }
                                }
                            },
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                selectedTextColor = Color.White,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(innerPadding)
        ) {

            // --- SHORTS ---
            composable(Routes.Home) {
                val viewModel = hiltViewModel<ShortsReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val shortsPrefix = "shorts"

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is ShortsEffect.OpenFilmDetail -> {
                                navController.navigate(Routes.detail(effect.film, shortsPrefix))
                            }
                        }
                    }
                }

                ShortsScreen(
                    state = state,
                    reducer = viewModel,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementPrefix = shortsPrefix
                )
            }

            // --- NEWS ---
            composable(Routes.News) {
                val viewModel = hiltViewModel<NewsReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val newsPrefix = "news"

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is NewsEffect.OpenFilmDetail -> {
                                navController.navigate(Routes.detail(effect.film, newsPrefix))
                            }
                        }
                    }
                }

                NewsScreen(
                    state = state,
                    reducer = viewModel,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementPrefix = newsPrefix
                )
            }

            // --- SEARCH ---
            composable(Routes.Search) {
                val viewModel = hiltViewModel<SearchReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val searchPrefix = "search"

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is SearchEffect.OpenFilmDetail -> {
                                navController.navigate(Routes.detail(effect.film, searchPrefix))
                            }
                        }
                    }
                }

                SearchScreen(
                    state = state,
                    reducer = viewModel,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementPrefix = searchPrefix
                )
            }

            // --- BOOKMARKS ---
            composable(Routes.Bookmarks) {
                val viewModel = hiltViewModel<BookmarksReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val bookmarksPrefix = "bookmarks"

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is BookmarksEffect.OpenFilmDetail -> {
                                navController.navigate(Routes.detail(effect.film, bookmarksPrefix))
                            }
                        }
                    }
                }

                BookmarksScreen(
                    state = state,
                    reducer = viewModel,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementPrefix = bookmarksPrefix
                )
            }

            // --- DETAILS ---
            composable(
                route = Routes.Detail,
                arguments = listOf(
                    navArgument("filmJson") { type = NavType.StringType },
                    navArgument("originPrefix") { type = NavType.StringType },
                )
            ) { backStackEntry ->
                val viewModel = hiltViewModel<FilmDetailReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val originPrefix = backStackEntry.arguments?.getString("originPrefix") ?: "unknown"

                val filmJson = backStackEntry.arguments?.getString("filmJson")
                val film = remember(filmJson) {
                    try {
                        Gson().fromJson(filmJson, Film::class.java)
                    } catch (e: Exception) {
                        Log.e("AppNavigation", "Error parsing film JSON", e)
                        null
                    }
                }
                val filmId = film?.id ?: -1
                val thumb = film?.thumbnailUrl ?: film?.backgroundImageUrl

                LaunchedEffect(Unit) {
                    if (film != null) {
                        viewModel.postAction(FilmDetailAction.SetInitialFilm(film))
                    }
                }

                FilmDetailScreen(
                    state = state,
                    reducer = viewModel,
                    cachedId = filmId,
                    cachedThumbnail = thumb,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    onBack = { navController.popBackStack() },
                    sharedElementPrefix = originPrefix,
                )
            }
        }
    }
}