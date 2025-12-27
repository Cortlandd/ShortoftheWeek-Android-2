package com.cortlandwalker.shortoftheweek.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.cortlandwalker.shortoftheweek.data.models.Film
import com.cortlandwalker.shortoftheweek.data.models.metadataLine
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import com.cortlandwalker.shortoftheweek.ui.theme.Futura

@Composable
fun FilmDetailPlayHeroOverlay(film: Film) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp, vertical = 28.dp),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Play circle (centered)
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Color.Black.copy(alpha = 0.55f), shape = androidx.compose.foundation.shape.CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(Modifier.height(14.dp))

        val meta = film.metadataLine
        if (!meta.isNullOrBlank()) {
            Text(
                text = meta.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontFamily = DomDiagonal
            )
            Spacer(Modifier.height(10.dp))
        }

        Text(
            text = film.title.uppercase(),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
            fontFamily = DomDiagonal
        )

        film.synopsis?.takeIf { it.isNotBlank() }?.let { synopsis ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.95f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                fontFamily = Futura
            )
        }
    }
}
