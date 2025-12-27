package com.cortlandwalker.shortoftheweek.core.helpers.article

import com.cortlandwalker.shortoftheweek.networking.FilmExternalLink

sealed class ArticleBlock {
    data class Heading(val level: Int, val text: String) : ArticleBlock()
    data class Paragraph(val text: String) : ArticleBlock()
    data class BulletedList(val bullets: List<String>) : ArticleBlock()
    data class BlockQuote(val text: String) : ArticleBlock()
    data class Image(val url: String, val caption: String?) : ArticleBlock()
    data object Divider : ArticleBlock()
    data class Links(val items: List<FilmExternalLink>) : ArticleBlock()
}
