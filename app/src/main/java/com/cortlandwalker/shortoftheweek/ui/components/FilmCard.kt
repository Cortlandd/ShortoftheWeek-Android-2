package com.cortlandwalker.shortoftheweek.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.CrossFade
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cortlandwalker.shortoftheweek.core.helpers.toSotwDisplayDateOrNull
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.headerCategoryLabel
import com.cortlandwalker.shortoftheweek.data.models.isNews
import com.cortlandwalker.shortoftheweek.networking.FilmTerm
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.Futura
import com.cortlandwalker.shortoftheweek.ui.theme.FuturaBold

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun FilmCard(
    film: Film,
    sharedKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope,
    onBookmarkClick: () -> Unit,
) {
    val imageUrl = remember(film) {
        if (film.kind == Film.Kind.NEWS) {
            film.thumbnailUrl ?: film.backgroundImageUrl
        } else {
            film.backgroundImageUrl ?: film.thumbnailUrl
        }
    }

    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clickable(onClick = onClick),
            shape = RectangleShape,
            colors = CardDefaults.cardColors(containerColor = Color.Black)
        ) {
            Box(Modifier.fillMaxSize()) {
                GlideImage(
                    model = imageUrl,
                    contentDescription = film.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .sharedElement(
                            sharedContentState = rememberSharedContentState(key = sharedKey),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                    contentScale = ContentScale.FillBounds,
                    transition = null
                )

                Box(
                    Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = 0.10f),
                                0.55f to Color.Black.copy(alpha = 0.60f),
                                1f to Color.Black.copy(alpha = 0.90f)
                            )
                        )
                )

                // Centered overlay content
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.widthIn(max = 420.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val meta = remember(film) { filmCardMetadataLine(film) }
                        if (!meta.isNullOrBlank()) {
                            Text(
                                text = meta.uppercase(),
                                color = Color.White.copy(alpha = 0.90f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = DomDiagonal,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = film.title.uppercase(),
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            fontFamily = FuturaBold,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (!film.synopsis.isNullOrBlank()) {
                            Text(
                                text = film.synopsis,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                fontFamily = Futura,
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onBookmarkClick,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        // Add circular background for visibility against any image/black bg
                        .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
                ) {
                    Icon(
                        imageVector = if (film.isBookmarked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Bookmark",
                        // Red if bookmarked, White outline if not
                        tint = if (film.isBookmarked) Color.Red else Color.White
                    )
                }
            }
        }
    }
}

/**
 * - NEWS: category / date
 * - else: genre / filmmaker / duration
 *
 * Adjust based on the exact Film fields you kept.
 */
private fun filmCardMetadataLine(film: Film): String? {
    val parts = mutableListOf<String>()

    if (film.isNews) {
        val cat = film.headerCategoryLabel
        if (!cat.isNullOrBlank()) parts += cat

        val fixedPostDate = film.postDate.toSotwDisplayDateOrNull()
        if (!fixedPostDate.isNullOrBlank()) {
            parts += fixedPostDate
        }
    } else {
        val genre = film.genreDisplayNameOrNull()
        if (!genre.isNullOrBlank()) parts += genre

        if (!film.filmmaker.isNullOrBlank()) parts += film.filmmaker

        val minutes = film.durationMinutes
        if (minutes != null && minutes > 0) {
            parts += if (minutes == 1) "1 MINUTE" else "$minutes MINUTES"
        }
    }

    return parts.takeIf { it.isNotEmpty() }?.joinToString(" / ")
}

private fun Film.genreDisplayNameOrNull(): String? = genre?.displayName

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun FilmCardHeroPreview() {
    MaterialTheme(colorScheme = darkColorScheme(background = Color.Black, surface = Color.Black)) {
        Surface(color = Color.Black) {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    FilmCard(
                        film = Film(
                            id = 1,
                            kind = Film.Kind.VIDEO,
                            title = "No Vacancy",
                            slug = "no-vacancy",
                            synopsis = "A restless mind drifts between thoughts...",
                            postDate = "2025-12-11",
                            backgroundImageUrl = "https://picsum.photos/900/500",
                            thumbnailUrl = "https://picsum.photos/900/500",
                            filmmaker = "Miguel Rodrick",
                            genre = FilmTerm(displayName = "Animation"),
                            production = "Short of the Week",
                            durationMinutes = 12,
                            playUrl = "https://example.com",
                            textColorHex = "#FFFFFF",
                            articleHtml = "<p>Preview</p>",
                            subscriptions = true,
                            isBookmarked = true
                        ),
                        sharedKey = "home-hero-1",
                        onClick = {},
                        animatedVisibilityScope = this,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onBookmarkClick = {}
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
private fun FilmCardHeroPreviewNews() {
    MaterialTheme(colorScheme = darkColorScheme(background = Color.Black, surface = Color.Black)) {
        Surface(color = Color.Black) {
            SharedTransitionLayout {
                AnimatedVisibility(visible = true) {
                    FilmCard(
                        film = Film(
                            id = 10,
                            kind = Film.Kind.NEWS,
                            title = "Festival Roundup",
                            slug = "festival-roundup",
                            synopsis = "Highlights from this week.",
                            postDate = "2024-09-05",
                            backgroundImageUrl = "https://picsum.photos/900/500",
                            thumbnailUrl = "https://picsum.photos/900/500",
                            filmmaker = null,
                            production = null,
                            durationMinutes = null,
                            playUrl = null,
                            textColorHex = "#FFFFFF",
                            articleHtml = "<p>Preview</p>"
                        ),
                        sharedKey = "home-hero-1",
                        onClick = {},
                        animatedVisibilityScope = this,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onBookmarkClick = {}
                    )
                }
            }
        }
    }
}
