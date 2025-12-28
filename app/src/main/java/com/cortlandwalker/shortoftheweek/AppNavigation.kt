package com.cortlandwalker.shortoftheweek

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cortlandwalker.shortoftheweek.core.navigation.Routes
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksEffect
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksReducer
import com.cortlandwalker.shortoftheweek.features.bookmarks.BookmarksScreen
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailAction
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailReducer
import com.cortlandwalker.shortoftheweek.features.detail.FilmDetailScreen
import com.cortlandwalker.shortoftheweek.features.home.HomeEffect
import com.cortlandwalker.shortoftheweek.features.home.HomeReducer
import com.cortlandwalker.shortoftheweek.features.home.HomeScreen
import com.cortlandwalker.shortoftheweek.features.news.NewsEffect
import com.cortlandwalker.shortoftheweek.features.news.NewsReducer
import com.cortlandwalker.shortoftheweek.features.news.NewsScreen
import com.cortlandwalker.shortoftheweek.features.search.SearchEffect
import com.cortlandwalker.shortoftheweek.features.search.SearchReducer
import com.cortlandwalker.shortoftheweek.features.search.SearchScreen
import com.google.gson.Gson

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    sharedTransitionScope: SharedTransitionScope
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Define when to show bottom bar (Hide on Detail screen)
    val showBottomBar = currentRoute != Routes.Detail

    Scaffold(
        bottomBar = {
            // Animate the bottom bar sliding in/out
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ) {
                    val items = listOf(
                        Triple(Routes.Home, "Home", Icons.Default.Home),
                        Triple(Routes.News, "News", Icons.AutoMirrored.Outlined.List),
                        Triple(Routes.Search, "Search", Icons.Default.Search),
                        Triple(Routes.Bookmarks, "Saved", Icons.Default.Favorite)
                    )

                    items.forEach { (route, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentRoute == route,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = Color.White,
                                indicatorColor = Color.White,
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray
                            ),
                            onClick = {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
        ) {

            // --- HOME ---
            composable(Routes.Home) {
                val viewModel = hiltViewModel<HomeReducer>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                val homePrefix = "home"

                LaunchedEffect(viewModel) {
                    viewModel.effect.collect { effect ->
                        when (effect) {
                            is HomeEffect.OpenFilmDetail -> {
                                navController.navigate(Routes.detail(effect.film, homePrefix))
                            }
                        }
                    }
                }

                HomeScreen(
                    state = state,
                    reducer = viewModel,
                    animatedVisibilityScope = this,
                    sharedTransitionScope = sharedTransitionScope,
                    sharedElementPrefix = homePrefix
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
