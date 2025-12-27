package com.cortlandwalker.shortoftheweek.core.helpers.article

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.cortlandwalker.shortoftheweek.ui.theme.DomDiagonal
import java.util.Locale
import java.util.Stack

private val BodyText = Color(0xFF272E2C)

@Composable
fun ArticleBlocksView(
    blocks: List<ArticleBlock>,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        blocks.forEachIndexed { index, block ->
            when (block) {
                is ArticleBlock.Heading -> {
                    val style = when (block.level) {
                        1 -> MaterialTheme.typography.headlineMedium
                        2 -> MaterialTheme.typography.headlineSmall
                        3 -> MaterialTheme.typography.titleLarge
                        else -> MaterialTheme.typography.titleMedium
                    }
                    Text(
                        text = block.text,
                        style = style,
                        color = Color(0xFF272E2C),
                        fontFamily = DomDiagonal,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = if (index == 0) 0.dp else 12.dp, bottom = 8.dp)
                    )
                }

                is ArticleBlock.Paragraph -> {
                    // Convert HTML-like string to AnnotatedString for display
                    val styledText = remember(block.text) { htmlToAnnotatedString(block.text) }

                    Text(
                        text = styledText,
                        color = BodyText,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is ArticleBlock.BlockQuote -> {
                    val styledText = remember(block.text) { htmlToAnnotatedString(block.text) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min) // Match height of text
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = styledText.toUpperCase(),
                            textAlign = TextAlign.Center,
                            color = BodyText.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontStyle = FontStyle.Italic
                            ),
                            fontFamily = DomDiagonal,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is ArticleBlock.BulletedList -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        block.bullets.forEach { bullet ->
                            val styledBullet = remember(bullet) { htmlToAnnotatedString(bullet) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("•", color = BodyText)
                                Text(
                                    text = styledBullet,
                                    color = BodyText,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                is ArticleBlock.Image -> {
                    ArticleImageBlock(url = block.url, caption = block.caption)
                }

                is ArticleBlock.Links -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        block.items.forEach { item ->
                            if (!item.url.isNullOrBlank()) {
                                Text(
                                    text = item.bestLabel ?: item.url.toString(),
                                    color = BodyText,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        textDecoration = TextDecoration.Underline
                                    ),
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                )
                            }
                        }
                    }
                }

                ArticleBlock.Divider -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .height(1.dp)
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
private fun ArticleImageBlock(url: String, caption: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        GlideImage(
            model = url,
            contentDescription = caption,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                color = BodyText.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Parses simple HTML tags (b, strong, i, em, u, span style) into a Compose AnnotatedString.
 */
private fun htmlToAnnotatedString(text: String): AnnotatedString {
    return buildAnnotatedString {
        val tokenizer = Regex(
            pattern = "(<(/?)(\\w+)([^>]*)>)|([^<]+)",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        val styleStack = Stack<SpanStyle>()

        tokenizer.findAll(text).forEach { result ->
            val fullTag = result.groups[1]?.value
            val textContent = result.groups[5]?.value

            if (textContent != null) {
                // Apply current styles
                var combinedStyle = SpanStyle()
                styleStack.forEach { combinedStyle = combinedStyle.merge(it) }

                withStyle(combinedStyle) {
                    append(textContent)
                }
            } else if (fullTag != null) {
                val isClosing = result.groups[2]?.value == "/"
                val tagName = result.groups[3]?.value?.lowercase(Locale.US)
                val attributes = result.groups[4]?.value ?: ""

                if (isClosing) {
                    if (styleStack.isNotEmpty()) styleStack.pop()
                } else {
                    val style = when (tagName) {
                        "b", "strong" -> SpanStyle(fontWeight = FontWeight.Bold)
                        "i", "em" -> SpanStyle(fontStyle = FontStyle.Italic)
                        "u" -> SpanStyle(textDecoration = TextDecoration.Underline)
                        "span" -> parseSpanStyle(attributes)
                        else -> null
                    }
                    styleStack.push(style ?: SpanStyle())
                }
            }
        }
    }
}

private fun parseSpanStyle(attributes: String): SpanStyle? {
    if (attributes.contains("text-decoration", ignoreCase = true)) {
        if (attributes.contains("underline", ignoreCase = true)) {
            return SpanStyle(textDecoration = TextDecoration.Underline)
        }
        if (attributes.contains("line-through", ignoreCase = true)) {
            return SpanStyle(textDecoration = TextDecoration.LineThrough)
        }
    }
    return null
}
