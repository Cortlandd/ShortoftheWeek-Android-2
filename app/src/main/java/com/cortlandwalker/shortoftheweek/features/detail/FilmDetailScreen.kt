package com.cortlandwalker.shortoftheweek.features.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.article.ArticleBlocksView
import com.cortlandwalker.shortoftheweek.core.helpers.article.ArticleParser
import com.cortlandwalker.shortoftheweek.core.helpers.decodeHtmlEntities
import com.cortlandwalker.shortoftheweek.core.helpers.fromHex
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.isNews
import com.cortlandwalker.shortoftheweek.ui.components.SotwEmptyState
import com.cortlandwalker.shortoftheweek.ui.components.SotwErrorState
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FilmDetailScreen(
    state: FilmDetailState,
    reducer: FilmDetailReducer,
    cachedId: Int,
    cachedThumbnail: String?,
    onBack: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    FilmDetailScreenContent(
        state = state,
        onRefresh = { reducer.postAction(FilmDetailAction.OnRefresh) },
        onPlay = { reducer.postAction(FilmDetailAction.OnPlayPressed) },
        onBack = onBack,
        cachedId = cachedId,
        cachedThumbnail = cachedThumbnail,
        animatedVisibilityScope = animatedVisibilityScope,
        sharedTransitionScope = sharedTransitionScope,
        onBookmarkToggle = { reducer.postAction(FilmDetailAction.OnBookmarkToggle) },
        sharedElementPrefix = sharedElementPrefix
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FilmDetailScreenContent(
    state: FilmDetailState,
    onRefresh: () -> Unit,
    onPlay: () -> Unit,
    onBack: () -> Unit,
    onBookmarkToggle: () -> Unit,
    cachedId: Int,
    cachedThumbnail: String?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    Scaffold(
        containerColor = Color.Black,
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onBookmarkToggle,
                    containerColor = Color.White,
                    contentColor = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val isBookmarked = state.film?.isBookmarked == true
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Save Film",
                        tint = if (isBookmarked) Color.Red else Color.Black
                    )
                }

                FloatingActionButton(
                    onClick = onBack,
                    containerColor = Color.White,
                    contentColor = Color.Black
                ) {
                    Icon(imageVector = Icons.Outlined.Close, contentDescription = "Back")
                }
            }
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.Black)
        ) {
            val displayFilm = state.film ?: Film(
                id = cachedId,
                kind = Film.Kind.VIDEO,
                title = "",
                slug = "",
                synopsis = "",
                postDate = "",
                backgroundImageUrl = cachedThumbnail,
                thumbnailUrl = cachedThumbnail,
                filmmaker = null,
                production = null,
                durationMinutes = null,
                playUrl = null,
                playLinkTarget = null,
                textColorHex = "#FFFFFF",
                twitterText = null,
                articleHtml = "",
                labels = emptyList(),
                subscriptions = false
            )

            when (val mode = state.viewDisplayMode) {
                is ViewDisplayMode.Error -> SotwErrorState(message = mode.message)

                ViewDisplayMode.Empty -> SotwEmptyState()

                else -> {
                    FilmDetailBody(
                        film = displayFilm,
                        isPlaying = state.isPlaying,
                        onPlay = onPlay,
                        animatedVisibilityScope = animatedVisibilityScope,
                        sharedTransitionScope = sharedTransitionScope,
                        sharedElementPrefix = sharedElementPrefix
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun FilmDetailBody(
    film: Film,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7E0DB))
            .verticalScroll(scroll)
    ) {
        HeroHeader(
            film = film,
            isPlaying = isPlaying,
            onPlay = onPlay,
            animatedVisibilityScope = animatedVisibilityScope,
            sharedTransitionScope = sharedTransitionScope,
            sharedElementPrefix = sharedElementPrefix
        )

        if (!film.isNews) {
            Topic(film = film)
            CreditsHeader(film = film)
        }
        DetailBody(film = film)
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalGlideComposeApi::class)
@Composable
private fun HeroHeader(
    film: Film,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    sharedElementPrefix: String
) {
    val imageUrl = film.backgroundImageUrl ?: film.thumbnailUrl
    val playUrl = film.playUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Color.Black)
    ) {
        if (!playUrl.isNullOrBlank() && isPlaying && !LocalInspectionMode.current) {
            key(playUrl) {
                FilmVideoEmbedView(
                    url = playUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            if (!imageUrl.isNullOrBlank()) {
                with(sharedTransitionScope) {
                    GlideImage(
                        model = imageUrl,
                        contentScale = ContentScale.FillBounds,
                        contentDescription = null,
                        transition = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                            .sharedElement(
                                sharedContentState = rememberSharedContentState(key = "$sharedElementPrefix-image-${film.id}"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                boundsTransform = { _, _ ->
                                    tween(durationMillis = 400)
                                }
                            )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.05f),
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.85f),
                            )
                        )
                    )
            )

            if (!playUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPlay() }
                )
            }

            if (film.kind == Film.Kind.NEWS) {
                FilmDetailNewsHeroOverlay(film = film)
            } else {
                if (!playUrl.isNullOrBlank()) {
                    FilmDetailPlayHeroOverlay(film = film)
                } else {
                    FilmDetailTitleOnlyHeroOverlay(film = film)
                }
            }
        }
    }
}

@Composable
private fun DetailBody(film: Film) {
    val blocks = ArticleParser.parse(film.articleHtml)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        if (film.isNews && !film.author?.displayName.isNullOrBlank()) {
            Text(
                text = film.author.displayName,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 10.dp)
            )
        }

        ArticleBlocksView(
            blocks = blocks,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FilmDetailTitleOnlyHeroOverlay(film: Film) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = film.title.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Topic(film: Film) {
    val secondaryColor = Color.fromHex("#95A6A1")
    val textStyle = TextStyle(
        fontFamily = DomDiagonal,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.fromHex("#647370"))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.Center
        ) {
            if (film.genre != null) {
                Text(
                    text = film.genre.displayName?.uppercase().toString(),
                    style = textStyle,
                    color = Color.fromHex(film.genre.color.toString())
                )
            }

            Text(
                text = "ABOUT",
                style = textStyle,
                color = secondaryColor
            )

            if (film.topic != null) {
                Text(
                    text = film.topic.displayName?.uppercase().toString(),
                    style = textStyle,
                    color = Color.fromHex(film.topic.color.toString())
                )
            }

            Text(
                text = "IN",
                style = textStyle,
                color = secondaryColor
            )

            if (film.style != null) {
                Text(
                    text = film.style.displayName?.uppercase().toString(),
                    style = textStyle,
                    color = Color.fromHex(film.style.color.toString())
                )
            }

        }
    }
}

@Composable
fun CreditsHeader(film: Film) {
    val creditColor = Color.fromHex("#D7E0DB")
    val textStyle = TextStyle(
        fontFamily = DomDiagonal,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        lineHeight = 20.sp
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.fromHex("#95A6A1"))
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        film.filmmaker?.takeIf { it.isNotEmpty() }?.let { director ->
            Text(
                text = "DIRECTED BY $director".decodeHtmlEntities().uppercase(),
                style = textStyle,
                color = creditColor
            )
        }

        film.production?.takeIf { it.isNotEmpty() }?.let { producer ->
            Text(
                text = "PRODUCED BY $producer".decodeHtmlEntities().uppercase(),
                style = textStyle,
                color = creditColor
            )
        }

        film.country?.displayName?.takeIf { it.isNotEmpty() }?.let { countryName ->
            Row {
                Text(
                    text = "MADE IN ",
                    style = textStyle,
                    color = creditColor
                )
                Text(
                    text = countryName.uppercase(),
                    style = textStyle,
                    color = if (!film.country.color.isNullOrBlank()) Color.fromHex(film.country.color) else creditColor
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun FilmDetailScreenPreview() {
    ShortOfTheWeekTheme {
        SharedTransitionLayout {
            AnimatedVisibility(visible = true) {
                // Mock State
                val sampleFilm = Film(
                    id = 1,
                    kind = Film.Kind.VIDEO,
                    title = "No Vacancy",
                    slug = "no-vacancy",
                    synopsis = "A restless mind drifts...",
                    postDate = "2025-12-11",
                    backgroundImageUrl = "https://picsum.photos/900/500",
                    thumbnailUrl = "https://picsum.photos/900/500",
                    filmmaker = "Miguel Rodrick",
                    genre = null,
                    production = "Short of the Week",
                    durationMinutes = 12,
                    playUrl = "https://example.com",
                    textColorHex = "#FFFFFF",
                    articleHtml = "<p>Preview Article</p>",
                    isBookmarked = false,
                )

                FilmDetailScreenContent(
                    state = FilmDetailState(
                        viewDisplayMode = ViewDisplayMode.Content,
                        film = sampleFilm
                    ),
                    onRefresh = {},
                    onPlay = {},
                    onBack = {},
                    onBookmarkToggle = {},
                    cachedId = 1,
                    cachedThumbnail = "https://picsum.photos/900/500",
                    animatedVisibilityScope = this,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    sharedElementPrefix = ""
                )
            }
        }
    }
}