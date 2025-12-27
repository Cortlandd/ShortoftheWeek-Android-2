package com.cortlandwalker.shortoftheweek.features.detail

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cortlandwalker.shortoftheweek.R
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.core.helpers.article.ArticleBlocksView
import com.cortlandwalker.shortoftheweek.core.helpers.article.ArticleParser
import com.cortlandwalker.shortoftheweek.core.helpers.decodeHtmlEntities
import com.cortlandwalker.shortoftheweek.core.helpers.fromHex
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.isNews
import com.cortlandwalker.shortoftheweek.ui.components.CenterMessage
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.Futura
import com.cortlandwalker.shortoftheweek.ui.theme.ShortOfTheWeekTheme

@Composable
fun FilmDetailScreen(state: FilmDetailState, reducer: FilmDetailReducer) {
    FilmDetailScreenContent(
        state = state,
        onRefresh = { reducer.postAction(FilmDetailAction.OnRefresh) },
        onPlay = { reducer.postAction(FilmDetailAction.OnPlayPressed) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreenContent(
    state: FilmDetailState,
    onRefresh: () -> Unit,
    onPlay: () -> Unit,
) {
    PullToRefreshBox(
        isRefreshing = state.isRefreshing,
        onRefresh = onRefresh,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (val mode = state.viewDisplayMode) {
            ViewDisplayMode.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is ViewDisplayMode.Error -> CenterMessage(mode.message)

            ViewDisplayMode.Empty -> {
                CenterMessage("Not found")
            }

            ViewDisplayMode.Content -> {
                val film = state.film
                if (film == null) {
                    CenterMessage("Not found")
                } else {
                    FilmDetailBody(
                        film = film,
                        isPlaying = state.isPlaying,
                        onPlay = onPlay
                    )
                }
            }
        }
    }
}

@Composable
private fun FilmDetailBody(film: Film, isPlaying: Boolean, onPlay: () -> Unit) {
    val scroll = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFD7E0DB))
            .verticalScroll(scroll)
    ) {
        HeroHeader(film = film, isPlaying = isPlaying, onPlay = onPlay)

        if (!film.isNews) {
            Topic(film = film)

            CreditsHeader(film = film)
        }

//        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
//            ArticleHtml(html = film.articleHtml)
//        }
        DetailBody(film = film)
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun HeroHeader(film: Film, isPlaying: Boolean, onPlay: () -> Unit) {
    val imageUrl = film.backgroundImageUrl ?: film.thumbnailUrl
    val playUrl = film.playUrl

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(Color.Black)
    ) {
        // Video revealed
        if (!playUrl.isNullOrBlank() && isPlaying && !LocalInspectionMode.current) {
            key(playUrl) {
                FilmVideoEmbedView(url = playUrl)
            }
        } else {
            // Thumbnail / background
            if (!imageUrl.isNullOrBlank()) {
                GlideImage(
                    model = imageUrl,
                    contentScale = ContentScale.FillBounds,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Gradient overlay like iOS
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

            // Tap anywhere to play (only if video exists)
            if (!playUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPlay() }
                )
            }

            // Overlay content (centered like your iOS screenshot)
            if (film.kind == Film.Kind.NEWS) {
                FilmDetailNewsHeroOverlay(film = film)
            } else {
                // video/article overlay with play button + metadata + title + synopsis
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
        // News author byline
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

// Shouldn't really ever be used
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
            // 1. Genre
            if (film.genre != null) {
                Text(
                    text = film.genre.displayName?.uppercase().toString(),
                    style = textStyle,
                    color = Color.fromHex(film.genre.color.toString())
                )
            }

            // 2. "ABOUT"
            Text(
                text = "ABOUT",
                style = textStyle,
                color = secondaryColor
            )

            // 3. Topic
            if (film.topic != null) {
                Text(
                    text = film.topic.displayName?.uppercase().toString(),
                    style = textStyle,
                    color = Color.fromHex(film.topic.color.toString())
                )
            }

            // 4. "IN"
            Text(
                text = "IN",
                style = textStyle,
                color = secondaryColor
            )

            // 5. Style
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
        // 1. Directed By
        film.filmmaker?.takeIf { it.isNotEmpty() }?.let { director ->
            Text(
                text = "DIRECTED BY $director".decodeHtmlEntities().uppercase(),
                style = textStyle,
                color = creditColor
            )
        }

        // 2. Produced By
        film.production?.takeIf { it.isNotEmpty() }?.let { producer ->
            Text(
                text = "PRODUCED BY $producer".decodeHtmlEntities().uppercase(),
                style = textStyle,
                color = creditColor
            )
        }

        // 3. Made In
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

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
private fun FilmDetailPreview() {
    ShortOfTheWeekTheme {
        FilmDetailScreenContent(
            state = FilmDetailState(
                filmId = 1,
                film = Film(
                    id = 1,
                    kind = Film.Kind.VIDEO,
                    title = "No Vacancy",
                    slug = "no-vacancy",
                    synopsis = "A restless, beautifully disorienting swirl.",
                    postDate = "2024-09-01",
                    backgroundImageUrl = "https://www.shortoftheweek.com/wp-content/uploads/2024/09/NoVacancy_Thumb.jpg",
                    thumbnailUrl = null,
                    filmmaker = "Miguel Rodrick",
                    production = null,
                    durationMinutes = 12,
                    playUrl = "https://www.shortoftheweek.com/",
                    textColorHex = "#FFFFFF",
                    articleHtml = "<p>Preview</p>"
                ),
                viewDisplayMode = ViewDisplayMode.Content
            ),
            onRefresh = {},
            onPlay = {}
        )
    }
}
