package com.cortlandwalker.shortoftheweek.features.detail

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cortlandwalker.shortoftheweek.core.helpers.ViewDisplayMode
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.ui.components.CenterMessage
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

            ViewDisplayMode.Empty -> CenterMessage("Not found")

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
            .verticalScroll(scroll)
    ) {
        HeroHeader(film = film, isPlaying = isPlaying, onPlay = onPlay)

        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(
                text = film.title,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = Color.White
            )

            val meta = listOfNotNull(
                film.filmmaker?.takeIf { it.isNotBlank() },
                film.durationMinutes?.let { "${'$'}it min" },
                film.postDate
            ).joinToString(" • ")
            if (meta.isNotBlank()) {
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                )
            }

            film.synopsis?.takeIf { it.isNotBlank() }?.let { synopsis ->
                Text(
                    text = synopsis,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.90f),
                    modifier = Modifier.padding(bottom = 14.dp)
                )
            }

            ArticleHtml(html = film.articleHtml)
        }
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
            .height(220.dp)
            .background(Color.Black)
    ) {
        if (!playUrl.isNullOrBlank() && isPlaying && !LocalInspectionMode.current) {
            VideoWebView(url = playUrl)
        } else {
            if (!imageUrl.isNullOrBlank()) {
                GlideImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (!playUrl.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onPlay() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun VideoWebView(url: String) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.BLACK)
            }
        },
        update = { webView ->
            if (webView.url != url) webView.loadUrl(url)
        }
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ArticleHtml(html: String) {
    if (html.isBlank()) return

    if (LocalInspectionMode.current) {
        Text(
            text = "(HTML preview omitted)",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )
        return
    }

    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = false
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
            }
        },
        update = { webView ->
            val wrapped = """
                <html>
                  <head>
                    <meta name='viewport' content='width=device-width, initial-scale=1.0' />
                    <style>
                      body { background: transparent; color: #FFFFFF; font-family: -apple-system, Roboto, sans-serif; }
                      a { color: #9BD0FF; }
                      img { max-width: 100%; height: auto; }
                      figure { margin: 0; }
                    </style>
                  </head>
                  <body>
                    ${'$'}html
                  </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL(null, wrapped, "text/html", "utf-8", null)
        }
    )
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
                    backgroundImageUrl = null,
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
