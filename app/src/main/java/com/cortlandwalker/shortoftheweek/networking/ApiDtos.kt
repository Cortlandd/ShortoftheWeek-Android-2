package com.cortlandwalker.shortoftheweek.networking

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*

/**
 * Top-level response from /api/v1/mixed (and news/search, same shape)
 */
@Serializable
data class FilmResponse(
    val count: Int = 0,
    val limit: Int = 0,
    val page: Int = 0,
    val total: Int = 0,
    @SerialName("page_max") val pageMax: Int = 0,
    @SerialName("_links") val links: FilmPageLinks? = null,
    val data: List<FilmItem> = emptyList()
)

@Parcelize
@Serializable
data class FilmPageLinks(
    // Use String? instead of URL to survive bad URLs.
    val first: String? = null,
    val last: String? = null,
    val next: String? = null,
    val previous: String? = null
) : Parcelable

/**
 * Category / country / style / topic, etc.
 */
@Parcelize
@Serializable
data class FilmTerm(
    @SerialName("ID") val id: Int? = null,
    val color: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    val slug: String? = null
) : Parcelable

@Parcelize
@Serializable
data class FilmTermCollection(
    val count: Int = 0,
    val limit: Int = 0,
    val page: Int = 0,
    val total: Int = 0,
    @SerialName("page_max") val pageMax: Int = 0,
    @SerialName("_links") val links: FilmPageLinks? = null,
    val data: List<FilmTerm> = emptyList()
) : Parcelable

@Parcelize
@Serializable
data class FilmAuthor(
    @SerialName("display_name") val displayName: String = "",
    @SerialName("first_name") val firstName: String? = null,
    @SerialName("last_name") val lastName: String? = null,
    @SerialName("ID") val id: String = "",
    val company: String? = null,
    val occupation: String? = null,
    val email: String? = null
) : Parcelable

/**
 * Built to handle bad URLs and label arrays
 */
@Parcelize
@Serializable
data class FilmExternalLink(
    val url: String? = null,
    @Serializable(with = StringOrStringArraySerializer::class)
    val label: @RawValue StringOrStringArray? = null
) : Parcelable {
    val bestLabel: String?
        get() = label?.first ?: label?.joined
}

@Parcelize
@Serializable
data class FilmItem(
    @SerialName("ID") val id: Int,
    @SerialName("post_author") val postAuthor: String = "",
    @SerialName("post_content") val postContentHTML: String = "",
    @SerialName("post_date") val postDateString: String = "",
    @SerialName("post_title") val postTitle: String = "",
    @SerialName("post_name") val postName: String = "",
    @SerialName("background_image") val backgroundImage: String? = null,

    val categories: FilmTermCollection? = null,
    val author: FilmAuthor? = null,
    val country: FilmTerm? = null,
    val filmmaker: String? = null,

    @Serializable(with = StringOrStringArraySerializer::class)
    val labels: @RawValue StringOrStringArray? = null,

    val links: List<FilmExternalLink>? = null,

    @SerialName("duration") val durationString: String? = null,
    val genre: FilmTerm? = null,

    @SerialName("play_link") val playLink: String? = null,
    @SerialName("play_link_target") val playLinkTarget: String? = null,

    @SerialName("post_excerpt") val postExcerpt: String? = null,
    val production: String? = null,
    val style: FilmTerm? = null,

    @Serializable(with = BoolOrArraySerializer::class)
    val subscriptions: @RawValue BoolOrArray? = null,

    val tags: FilmTermCollection? = null,
    @SerialName("text_color") val textColor: String? = null,
    @SerialName("twitter_text") val twitterText: String? = null,
    val thumbnail: String? = null,

    val type: String = "", // "video", "article", etc.

    val topic: FilmTerm? = null
) : Parcelable

/**
 * Because strangely it can be a boolean or an array. Weird.
 * Mirrors your Swift BoolOrArray.
 */
@Serializable
sealed class BoolOrArray {
    data class Bool(val value: Boolean) : BoolOrArray()
    data class ArrayCount(val count: Int) : BoolOrArray()

    val asBool: Boolean
        get() = when (this) {
            is Bool -> value
            is ArrayCount -> count > 0
        }
}

object BoolOrArraySerializer : KSerializer<BoolOrArray> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BoolOrArray")

    override fun deserialize(decoder: Decoder): BoolOrArray {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return BoolOrArray.Bool(false)

        val el = jsonDecoder.decodeJsonElement()
        return when (el) {
            is JsonPrimitive -> {
                el.booleanOrNull?.let { BoolOrArray.Bool(it) } ?: BoolOrArray.Bool(false)
            }
            is JsonArray -> BoolOrArray.ArrayCount(el.size)
            else -> BoolOrArray.Bool(false)
        }
    }

    override fun serialize(encoder: Encoder, value: BoolOrArray) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: return

        val el: JsonElement = when (value) {
            is BoolOrArray.Bool -> JsonPrimitive(value.value)
            is BoolOrArray.ArrayCount -> JsonArray(List(value.count) { JsonNull })
        }
        jsonEncoder.encodeJsonElement(el)
    }
}

/**
 * Decodes either:
 * - "News"
 * - ["News", "Interviews"]
 * - null / missing
 */
@Serializable
data class StringOrStringArray(val values: List<String>) {
    val joined: String get() = values.joinToString(" / ")
    val first: String? get() = values.firstOrNull()
}

object StringOrStringArraySerializer : KSerializer<StringOrStringArray> {
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("StringOrStringArray")

    override fun deserialize(decoder: Decoder): StringOrStringArray {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return StringOrStringArray(emptyList())

        val el = jsonDecoder.decodeJsonElement()
        return when (el) {
            is JsonNull -> StringOrStringArray(emptyList())
            is JsonPrimitive -> StringOrStringArray(el.contentOrNull?.let { listOf(it) } ?: emptyList())
            is JsonArray -> {
                val vals = el.mapNotNull { it.jsonPrimitive.contentOrNull }.filter { it.isNotBlank() }
                StringOrStringArray(vals)
            }
            else -> StringOrStringArray(emptyList())
        }
    }

    override fun serialize(encoder: Encoder, value: StringOrStringArray) {
        val jsonEncoder = encoder as? JsonEncoder ?: return
        val el: JsonElement =
            if (value.values.size == 1) JsonPrimitive(value.values[0])
            else JsonArray(value.values.map { JsonPrimitive(it) })
        jsonEncoder.encodeJsonElement(el)
    }
}

/**
 * StringOrInt in Swift.
 */
@Serializable
data class StringOrInt(val value: String)

object StringOrIntSerializer : KSerializer<StringOrInt> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("StringOrInt", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): StringOrInt {
        val jsonDecoder = decoder as? JsonDecoder
            ?: return StringOrInt("")

        val el = jsonDecoder.decodeJsonElement()
        val v = when (el) {
            is JsonPrimitive -> el.contentOrNull ?: ""
            else -> ""
        }
        return StringOrInt(v)
    }

    override fun serialize(encoder: Encoder, value: StringOrInt) {
        encoder.encodeString(value.value)
    }
}

/**
 * AnyDecodable equivalent — if you need “decode whatever”, use JsonElement.
 * (This is better than a dummy type in Kotlin.)
 */
typealias AnyDecodable = JsonElement
