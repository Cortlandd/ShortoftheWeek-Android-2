package com.cortlandwalker.shortoftheweek.features.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cortlandwalker.shortoftheweek.core.helpers.decodeHtmlEntities
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.metadataLine
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.FuturaBold

@Composable
fun FilmDetailNewsHeroOverlay(film: Film) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 28.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val meta = film.metadataLine
        if (!meta.isNullOrBlank()) {
            Text(
                text = meta.decodeHtmlEntities().uppercase(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = DomDiagonal
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = film.title.decodeHtmlEntities().uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontFamily = DomDiagonal,
            textAlign = TextAlign.Center
        )

        film.author?.displayName?.let {
            Text(
                film.author.displayName.uppercase(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.95f),
                overflow = TextOverflow.Ellipsis,
                fontFamily = FuturaBold
            )
        }
    }
}
