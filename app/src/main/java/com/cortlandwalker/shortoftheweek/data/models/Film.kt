package com.cortlandwalker.shortoftheweek.data.models

import android.os.Parcelable
import androidx.room.util.copy
import com.cortlandwalker.shortoftheweek.core.helpers.toSotwDisplayDateOrNull
import com.cortlandwalker.shortoftheweek.networking.BoolOrArray
import com.cortlandwalker.shortoftheweek.networking.FilmAuthor
import com.cortlandwalker.shortoftheweek.networking.FilmExternalLink
import com.cortlandwalker.shortoftheweek.networking.FilmItem
import com.cortlandwalker.shortoftheweek.networking.FilmTerm
import com.cortlandwalker.shortoftheweek.networking.FilmTermCollection
import com.cortlandwalker.shortoftheweek.networking.StringOrStringArray
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Film(
    val id: Int,
    val kind: Kind,
    val title: String,
    val slug: String,
    val synopsis: String?,
    val postDate: String?,
    val backgroundImageUrl: String?,
    val thumbnailUrl: String?,
    val filmmaker: String?,
    val production: String?,
    val durationMinutes: Int?,
    val playUrl: String?,
    val playLinkTarget: String? = null,
    val textColorHex: String?,
    val twitterText: String? = null,
    val articleHtml: String,

    val postAuthorRaw: String = "",
    val author: FilmAuthor? = null,
    val categories: FilmTermCollection? = null,
    val tags: FilmTermCollection? = null,
    val labels: List<String> = emptyList(),
    val links: List<FilmExternalLink>? = null,
    val country: FilmTerm? = null,
    val topic: FilmTerm? = null,
    val genre: FilmTerm? = null,
    val style: FilmTerm? = null,
    val subscriptions: Boolean? = null,
    val isBookmarked: Boolean = false,
): Parcelable {
    enum class Kind { VIDEO, ARTICLE, NEWS, UNKNOWN }

    companion object {
        fun from(item: FilmItem): Film {
            val t = item.type.trim().lowercase()
            val kind = when {
                t == "news" -> Kind.NEWS
                t == "video" || !item.playLink.isNullOrBlank() -> Kind.VIDEO
                t == "article" -> Kind.ARTICLE
                else -> Kind.UNKNOWN
            }

            return Film(
                id = item.id,
                kind = kind,
                title = item.postTitle,
                slug = item.postName,
                synopsis = item.postExcerpt,
                postDate = item.postDateString,
                backgroundImageUrl = normalizeUrl(item.backgroundImage),
                thumbnailUrl = normalizeUrl(item.thumbnail),
                filmmaker = item.filmmaker,
                production = item.production,
                durationMinutes = item.durationString?.trim()?.toIntOrNull(),
                playUrl = normalizeUrl(item.playLink),
                playLinkTarget = item.playLinkTarget,
                textColorHex = item.textColor,
                twitterText = item.twitterText,
                articleHtml = item.postContentHTML,

                postAuthorRaw = item.postAuthor,
                author = item.author,
                categories = item.categories,
                tags = item.tags,
                labels = item.labels?.values ?: emptyList(),
                links = item.links,
                country = item.country,
                topic = item.topic,
                genre = item.genre,
                style = item.style,
                subscriptions = item.subscriptions?.asBool
            )
        }
    }
}

val Film.isNews: Boolean
    get() = kind == Film.Kind.NEWS

val Film.headerCategoryLabel: String?
    get() = categories?.data?.firstOrNull()?.displayName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.uppercase()

/**
 * Same idea as iOS metadataLine for cards.
 * Keep this in domain so UI stays dumb.
 */
val Film.metadataLine: String?
    get() {
        val parts = mutableListOf<String>()

        if (isNews) {
            headerCategoryLabel?.let(parts::add)
            postDate.toSotwDisplayDateOrNull()?.let(parts::add)
        } else {
            genre?.displayName?.trim()?.takeIf { it.isNotBlank() }?.let(parts::add)
            filmmaker?.trim()?.takeIf { it.isNotBlank() }?.let(parts::add)
            durationMinutes?.takeIf { it > 0 }?.let { m ->
                parts.add(if (m == 1) "1 MINUTE" else "$m MINUTES")
            }
        }

        return parts.takeIf { it.isNotEmpty() }?.joinToString(" / ")
    }

fun Film.normalized(): Film = copy(
    backgroundImageUrl = normalizeUrl(backgroundImageUrl),
    thumbnailUrl = normalizeUrl(thumbnailUrl),
    playUrl = normalizeUrl(playUrl)
)

private fun normalizeUrl(raw: String?): String? {
    val s = raw?.trim().orEmpty()
    if (s.isBlank()) return null
    return when {
        s.startsWith("https://") || s.startsWith("http://") -> s
        s.startsWith("//") -> "https:$s"
        s.startsWith("/") -> "https://static.shortoftheweek.com$s"
        else -> s
    }
}
