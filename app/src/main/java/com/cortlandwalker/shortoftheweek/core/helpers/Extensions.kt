package com.cortlandwalker.shortoftheweek.core.helpers

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone
import kotlin.math.absoluteValue

fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

fun String?.toSotwDisplayDateOrNull(): String? {
    val raw = this?.trim().orEmpty()
    if (raw.isBlank()) return null

    return runCatching {
        val outFmt = SimpleDateFormat("MMMM d, yyyy", Locale.US)

        // Try "yyyy-MM-dd HH:mm:ss" first (UTC), then "yyyy-MM-dd" (date-only).
        val formats = listOf(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            },
            SimpleDateFormat("yyyy-MM-dd", Locale.US) // no time; interpret as local date
        )

        val date = formats.firstNotNullOfOrNull { fmt ->
            runCatching { fmt.parse(raw) }.getOrNull()
        } ?: return null

        outFmt.format(date)
    }.getOrNull()
}

/**
 * Prevents HTTP 414 (URI too long) by bounding the search query.
 */
fun String.normalizeSearchQueryOrNull(maxLen: Int = 160): String? {
    val normalized = trim().replace(Regex("\\s+"), " ")
    if (normalized.isBlank()) return null
    return if (normalized.length <= maxLen) normalized else normalized.take(maxLen)
}

fun Color.Companion.fromHex(hex: String): Color {
    val cleanedHex = hex.replace("#", "")
    val int = cleanedHex.toLong(16)
    var a: Long
    var r: Long
    var g: Long
    var b: Long

    // Use cleanedHex.length for the when statement
    when (cleanedHex.length) {
        3 -> {
            a = 255
            r = (int shr 8) * 17
            g = (int shr 4 and 0xF) * 17
            b = (int and 0xF) * 17
        }
        6 -> {
            a = 255
            r = int shr 16
            g = int shr 8 and 0xFF
            b = int and 0xFF
        }
        8 -> {
            a = int shr 24
            r = int shr 16 and 0xFF
            g = int shr 8 and 0xFF
            b = int and 0xFF
        }
        else -> {
            // Handle invalid hex string by returning a default color (e.g., black)
            a = 255
            r = 0
            g = 0
            b = 0
        }
    }

    return Color(
        red = r.toFloat() / 255f,
        green = g.toFloat() / 255f,
        blue = b.toFloat() / 255f,
        alpha = a.toFloat() / 255f
    )
}

// ---- String Helpers ----

/**
 * Cleans text but KEEPS <strong>, <b>, <em>, <i>, <u> and style="..." tags
 * so the UI can parse them.
 */
fun String.preservingRichText(): String {
    var s = this

    // Convert <br> to newline
    s = s.replace(Regex("(?i)<br\\s*/?>"), "\n")

    // We want to remove all tags EXCEPT: strong, b, em, i, u, span
    // Regex explanation: Matches <(?!/?(b|strong|i|em|u|span)...)>
    val removeTagsRegex = Regex("<(?!/?(b|strong|i|em|u|span)\\b)[^>]+>", RegexOption.IGNORE_CASE)
    s = s.replace(removeTagsRegex, "")

    // Decode entities
    return s.decodeHtmlEntities()
        .replace("\r\n", "\n")
        .replace("\r", "\n")
        .replace(Regex("[ \\t]{2,}"), " ")
        .replace(Regex(" *\n *"), "\n")
        .trim()
}

/** Completely strips all tags (used for headings/captions) */
fun String.strippingAllTags(): String {
    return this.replace(Regex("<[^>]+>"), "")
        .replace(Regex("(?i)<br\\s*/?>"), "\n")
        .decodeHtmlEntities()
        .replace(Regex("\\s+"), " ")
        .trim()
}

fun String.decodeHtmlEntities(): String {
    return this
        .replace("&nbsp;", " ")
        .replace("\u00A0", " ")
        .replace("&amp;", "&")
        .replace("&lt;", "<")
        .replace("&gt;", ">")
        .replace("&quot;", "\"")
        .replace("&apos;", "'")
        .replace("&ndash;", "–")
        .replace("&mdash;", "—")
}
