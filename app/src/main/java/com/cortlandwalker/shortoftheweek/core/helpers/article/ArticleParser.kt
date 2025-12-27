package com.cortlandwalker.shortoftheweek.core.helpers.article

import com.cortlandwalker.shortoftheweek.core.helpers.preservingRichText
import com.cortlandwalker.shortoftheweek.core.helpers.strippingAllTags
import java.util.Locale

object ArticleParser {

    fun parse(html: String): List<ArticleBlock> {
        val blocks = mutableListOf<ArticleBlock>()
        if (html.isBlank()) return blocks

        // [caption]...[/caption] handling (unchanged)
        val captionRegex = Regex(
            pattern = "\\[caption[^\\]]*\\](.*?)\\[/caption\\]",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        var currentIndex = 0
        val matches = captionRegex.findAll(html).toList()

        for (m in matches) {
            val matchStart = m.range.first
            val matchEndExclusive = m.range.last + 1
            val before = html.substring(currentIndex, matchStart)
            appendBlocks(fromHtml = before, into = blocks)
            val inner = m.groups[1]?.value.orEmpty()
            parseCaptionBlock(inner)?.let { blocks.add(it) }
            currentIndex = matchEndExclusive
        }
        val remaining = html.substring(currentIndex)
        appendBlocks(fromHtml = remaining, into = blocks)

        return blocks
    }

    private fun appendBlocks(fromHtml: String, into: MutableList<ArticleBlock>) {
        val html = fromHtml
        if (html.isBlank()) return

        // ADDED: blockquote to the regex
        val blockRegex = Regex(
            pattern = "<(h[1-6]|p|ul|ol|figure|blockquote)[^>]*>(.*?)</\\1>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )

        val matches = blockRegex.findAll(html).toList()

        if (matches.isEmpty()) {
            appendLooseTextAsParagraph(html, into)
            return
        }

        var cursor = 0

        for (m in matches) {
            val wholeStart = m.range.first
            val wholeEndExclusive = m.range.last + 1

            // Loose text before match
            val between = html.substring(cursor, wholeStart)
            appendLooseTextAsParagraph(between, into)

            val tag = m.groups[1]?.value.orEmpty().lowercase(Locale.US)
            val inner = m.groups[2]?.value.orEmpty()

            when {
                tag.startsWith("h") -> {
                    val level = tag.drop(1).toIntOrNull() ?: 2
                    val text = inner.strippingAllTags() // Headings usually don't need bold/rich text
                    if (text.isNotBlank()) into.add(ArticleBlock.Heading(level = level, text = text))
                }

                tag == "p" -> {
                    // CHANGED: Use preservingRichText instead of strippingSimpleHTML
                    val text = inner.preservingRichText()
                    if (text.isNotBlank()) into.add(ArticleBlock.Paragraph(text))
                }

                tag == "blockquote" -> {
                    // Blockquotes often contain <p> tags. We strip the p tags but keep the text content + inline styles.
                    val cleanInner = inner.replace(Regex("<p[^>]*>"), "").replace("</p>", "<br>")
                    val text = cleanInner.preservingRichText()
                    if (text.isNotBlank()) into.add(ArticleBlock.BlockQuote(text))
                }

                tag == "ul" || tag == "ol" -> {
                    val items = parseListItems(inner)
                    if (items.isNotEmpty()) into.add(ArticleBlock.BulletedList(items))
                }

                tag == "figure" -> {
                    parseFigureBlock(inner)?.let { into.add(it) }
                }
            }
            cursor = wholeEndExclusive
        }

        val tail = html.substring(cursor)
        appendLooseTextAsParagraph(tail, into)
    }

    private fun parseListItems(html: String): List<String> {
        val liRegex = Regex(
            pattern = "<li[^>]*>(.*?)</li>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        // List items also preserve rich text now
        return liRegex.findAll(html).map { m ->
            m.groups[1]?.value.orEmpty().preservingRichText()
        }.filter { it.isNotBlank() }.toList()
    }

    private fun appendLooseTextAsParagraph(html: String, into: MutableList<ArticleBlock>) {
        if (html.isBlank()) return

        val imgRegex = Regex(
            pattern = "<img\\b[^>]*>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val matches = imgRegex.findAll(html).toList()

        if (matches.isEmpty()) {
            val text = html.preservingRichText()
            if (text.isNotBlank()) into.add(ArticleBlock.Paragraph(text))
            return
        }

        var cursor = 0
        for (m in matches) {
            val start = m.range.first
            val endExclusive = m.range.last + 1

            val before = html.substring(cursor, start)
            val beforeText = before.preservingRichText()
            if (beforeText.isNotBlank()) into.add(ArticleBlock.Paragraph(beforeText))

            val imgTag = html.substring(start, endExclusive)
            extractImgSrc(imgTag)?.let { url ->
                into.add(ArticleBlock.Image(url = url, caption = null))
            }
            cursor = endExclusive
        }

        val tail = html.substring(cursor)
        val tailText = tail.preservingRichText()
        if (tailText.isNotBlank()) into.add(ArticleBlock.Paragraph(tailText))
    }

    // ... (extractImgSrc, parseCaptionBlock, parseFigureBlock, parseImageUrl, normalizeUrl remain the same) ...

    private fun extractImgSrc(imgTag: String): String? {
        val srcRegex = Regex(
            pattern = "\\bsrc\\s*=\\s*(['\"])(.*?)\\1",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val m = srcRegex.find(imgTag) ?: return null
        val raw = m.groups[2]?.value ?: return null
        return normalizeUrl(raw)
    }

    private fun parseCaptionBlock(html: String): ArticleBlock? {
        val imgRegex = Regex(
            pattern = "<img[^>]*src=\"([^\"]+)\"[^>]*>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val m = imgRegex.find(html) ?: return null
        val src = m.groups[1]?.value ?: return null

        val captionHtml = html.replaceRange(m.range, "")
        val caption = captionHtml.strippingAllTags().trim().ifBlank { null }

        val url = normalizeUrl(src) ?: return null
        return ArticleBlock.Image(url = url, caption = caption)
    }

    private fun parseFigureBlock(html: String): ArticleBlock? {
        val url = parseImageUrl(html) ?: return null
        val capRegex = Regex(
            pattern = "<figcaption[^>]*>(.*?)</figcaption>",
            options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
        )
        val cap = capRegex.find(html)?.groups?.get(1)?.value?.strippingAllTags()?.trim()?.ifBlank { null }
        return ArticleBlock.Image(url = url, caption = cap)
    }

    private fun parseImageUrl(html: String): String? {
        fun firstAttr(name: String): String? {
            val attrRegex = Regex(
                pattern = "\\b${Regex.escape(name)}\\s*=\\s*(['\"])(.*?)\\1",
                options = setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
            )
            return attrRegex.find(html)?.groups?.get(2)?.value
        }
        val raw = firstAttr("src") ?: firstAttr("data-src") ?: firstAttr("data-lazy-src") ?: firstAttr("data-original")
        if (raw != null) return normalizeUrl(raw)

        // ... (srcset logic remains same if needed) ...
        return null
    }

    private fun normalizeUrl(raw: String): String? {
        val s = raw.trim()
        if (s.isBlank()) return null
        return if (s.startsWith("//")) "https:$s" else s
    }
}
