package com.cortlandwalker.shortoftheweek.features.detail.article

sealed interface ArticleBlock {
    data class Header(val text: String, val level: Int) : ArticleBlock
    data class Paragraph(val text: String) : ArticleBlock
    data class Quote(val text: String) : ArticleBlock
    data class BulletedList(val bullets: List<String>) : ArticleBlock
    data class NumberedList(val items: List<String>) : ArticleBlock
    data class Image(val url: String, val alt: String?) : ArticleBlock
    data class HorizontalRule(val dummy: Unit = Unit) : ArticleBlock
}
