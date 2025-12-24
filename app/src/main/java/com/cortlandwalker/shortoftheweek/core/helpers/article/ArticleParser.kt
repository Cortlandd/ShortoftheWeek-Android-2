package com.cortlandwalker.shortoftheweek.features.detail.article

import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object ArticleParser {

    fun parse(html: String): List<ArticleBlock> {
        if (html.isBlank()) return emptyList()

        val doc = Jsoup.parseBodyFragment(html)
        val blocks = mutableListOf<ArticleBlock>()

        fun emitFrom(el: Element) {
            when (el.tagName().lowercase()) {
                "h1","h2","h3","h4","h5","h6" -> {
                    val level = el.tagName().substring(1).toIntOrNull() ?: 2
                    val text = el.text().trim()
                    if (text.isNotBlank()) blocks += ArticleBlock.Header(text, level)
                }
                "p" -> {
                    val text = el.text().trim()
                    if (text.isNotBlank()) blocks += ArticleBlock.Paragraph(text)
                }
                "blockquote" -> {
                    val text = el.text().trim()
                    if (text.isNotBlank()) blocks += ArticleBlock.Quote(text)
                }
                "ul" -> {
                    val bullets = el.select("> li").map { it.text().trim() }.filter { it.isNotBlank() }
                    if (bullets.isNotEmpty()) blocks += ArticleBlock.BulletedList(bullets)
                }
                "ol" -> {
                    val items = el.select("> li").map { it.text().trim() }.filter { it.isNotBlank() }
                    if (items.isNotEmpty()) blocks += ArticleBlock.NumberedList(items)
                }
                "img" -> {
                    val url = el.attr("src").trim()
                    if (url.isNotBlank()) blocks += ArticleBlock.Image(url, el.attr("alt").ifBlank { null })
                }
                "hr" -> blocks += ArticleBlock.HorizontalRule()
                else -> el.children().forEach(::emitFrom)
            }
        }

        doc.body().children().forEach(::emitFrom)
        return blocks
    }
}
