package com.cortlandwalker.shortoftheweek.core.helpers

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.TimeZone

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