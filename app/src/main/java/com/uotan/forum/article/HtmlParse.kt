package com.uotan.forum.article

import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.URLSpan
import com.uotan.forum.utils.UotanLinkSpan
import org.jsoup.nodes.*
import org.jsoup.Jsoup

object HtmlParse {

    fun parse(html: String): MutableList<ContentBlock> {
        val doc: Document = Jsoup.parse(html)
        val body = doc.body()
        return parseNodes(body.childNodes())
    }

    private fun parseNodes(nodes: List<Node>, allowTable: Boolean = true): MutableList<ContentBlock> {
        val blocks = mutableListOf<ContentBlock>()
        val currentText = StringBuilder()

        for (node in nodes) {
            when (node) {
                is Element -> {
                    when (node.tagName()) {
                        "a" -> {
                            handleLink(node, blocks, currentText)
                        }
                        "img" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            handleImage(node, blocks)
                        }
                        "span" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            handleMediaEmbed(node, blocks)
                        }
                        "iframe" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            handleIframe(node, blocks)
                        }
                        "br" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                        }
                        "div", "p" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            // 递归处理容器
                            blocks.addAll(parseNodes(node.childNodes()))
                        }
                        "ul" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            // 处理无序列表
                            val listItems = node.getElementsByTag("li")
                            for (item in listItems) {
                                val itemBlocks = parseNodes(item.childNodes())

                                val textContent = itemBlocks.joinToString(" ") {
                                    when(it) {
                                        is ContentBlock.TextBlock -> it.html
                                        else -> "<br>此内容嵌套层数过多，请前往网页端查看"
                                    }
                                }
                                blocks.add(ContentBlock.TextBlock("· $textContent")) // 无序列表项加圆点前缀
                            }
                        }
                        "ol" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            // 处理有序列表
                            val listItems = node.getElementsByTag("li")
                            var counter = 1
                            for (item in listItems) {
                                val itemBlocks = parseNodes(item.childNodes(), false)
                                val textContent = itemBlocks.joinToString("") {
                                    when(it) {
                                        is ContentBlock.TextBlock -> it.html
                                        else -> "<br>此内容嵌套层数过多，请前往网页端查看"
                                    }
                                }
                                blocks.add(ContentBlock.TextBlock("${counter}. $textContent")) // 有序列表项加数字前缀
                                counter++
                            }
                        }
                        "li" -> {
                            // 直接处理孤立的 li 标签
                            flushText(currentText, blocks)
                            blocks.addAll(parseNodes(node.childNodes(), false))
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            handleHeading(node, blocks)
                        }
                        "table" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            if (allowTable) handleTable(node, blocks) else node.html()
                        }
                        "tr" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            if (allowTable) handleTableRow(node, blocks) else node.html()
                        }
                        "td", "th" -> {
                            // 处理累积文本
                            flushText(currentText, blocks)
                            if (allowTable) handleTableCell(node, blocks) else node.html()
                        }
                        else -> {
                            // 对于其他标签，提取其文本内容，并且此分支不处理累积文本，直到遇到软件可处理的元素标签
                            val text = node.outerHtml()
                            if (text.isNotEmpty()) {
                                currentText.append(text)
                            }
                        }
                    }
                }
                is TextNode -> {
                    val text = node.text().trim()
                    if (text.isNotEmpty()) {
                        // 累积文本节点，直到遇到元素标签
                        if (currentText.isNotEmpty()) currentText.append(" ")
                        currentText.append(text)
                    }
                }
            }
        }
        flushText(currentText, blocks) // 处理最后剩余的文本
        return blocks
    }

    private fun handleHeading(element: Element, blocks: MutableList<ContentBlock>) {
        val level = when (element.tagName().lowercase()) {
            "h1" -> 1
            "h2" -> 2
            "h3" -> 3
            "h4" -> 4
            "h5" -> 5
            "h6" -> 6
            else -> 1 // 默认为h1
        }
        val text = element.text().trim()
        if (text.isNotEmpty()) {
            blocks.add(ContentBlock.HeadingBlock(level, text))
        }
    }

    private fun handleLink(element: Element, blocks: MutableList<ContentBlock>, currentText: StringBuilder) {
        // 处理包含图片的链接
        val images = element.select("img")
        if (images.isNotEmpty()) {
            flushText(currentText, blocks)
            images.forEach { img ->
                val src = img.attr("src")
                val alt = img.attr("alt")
                if (src.isNotBlank()) {
                    blocks.add(ContentBlock.ImageBlock(src, alt))
                }
            }
        } else {
            val text = element.outerHtml()
            if (text.isNotEmpty()) {
                currentText.append(text)
            }
        }
    }

    private fun flushText(
        currentText: StringBuilder,
        blocks: MutableList<ContentBlock>
    ) {
        if (currentText.isNotEmpty()) {
            val spanned = Html.fromHtml(
                currentText.toString(),
                Html.FROM_HTML_MODE_COMPACT
            )
            val spannable = spanned as? SpannableStringBuilder ?: SpannableStringBuilder(spanned)
            val urlSpans = spannable.getSpans(0, spannable.length, URLSpan::class.java)
            for (span in urlSpans) {
                val start = spannable.getSpanStart(span)
                val end = spannable.getSpanEnd(span)
                val flags = spannable.getSpanFlags(span)
                val url = span.url
                // 移除原有的URLSpan
                spannable.removeSpan(span)
                // 添加自定义的Span
                val noUnderlineSpan = UotanLinkSpan(url)
                spannable.setSpan(noUnderlineSpan, start, end, flags)
            }
            blocks.add(ContentBlock.TextBlock(spanned))
            currentText.clear()
        }
    }

    private fun handleImage(element: Element, blocks: MutableList<ContentBlock>) {
        val src = element.attr("src")
        val alt = element.attr("alt")
        if (src.isNotBlank()) {
            blocks.add(ContentBlock.ImageBlock(src, alt))
        }
    }

    private fun handleMediaEmbed(element: Element, blocks: MutableList<ContentBlock>) {
        // 处理自定义媒体嵌入（如B站视频）
        val mediaType = element.attr("data-guineapigclub-mediaembed")
        if (mediaType.isNotBlank()) {
            val iframe = element.select("iframe").first()
            if (iframe != null) {
                val src = iframe.attr("src")
                if (src.isNotBlank()) {
                    blocks.add(ContentBlock.IframeBlock(src))
                }
            }
        } else {
            // 普通span当作文本处理
            val text = element.text().trim()
            if (text.isNotEmpty()) {
                blocks.add(ContentBlock.TextBlock(text))
            }
        }
    }

    private fun handleIframe(element: Element, blocks: MutableList<ContentBlock>) {
        val src = element.attr("src")
        if (src.isNotBlank()) {
            blocks.add(ContentBlock.IframeBlock(src))
        }
    }

    private fun handleTable(element: Element, blocks: MutableList<ContentBlock>) {
        val table = ContentBlock.TableBlock(mutableListOf())
        element.select("tr").forEach { tr ->
            val row = ContentBlock.TableRowBlock(mutableListOf())
            tr.select("td, th").forEach { cell ->
                val cellBlocks = parseNodes(cell.childNodes(), allowTable = false)
                row.cells.add(ContentBlock.TableCellBlock(cellBlocks))
            }
            table.rows.add(row)
        }
        blocks.add(table)
    }

    private fun handleTableRow(element: Element, blocks: MutableList<ContentBlock>) {
        // 单独的行处理（用于非table包裹的tr）
        val row = ContentBlock.TableRowBlock(mutableListOf())
        element.select("td, th").forEach { cell ->
            val cellBlocks = parseNodes(cell.childNodes(), allowTable = false)
            row.cells.add(ContentBlock.TableCellBlock(cellBlocks))
        }
        blocks.add(row)
    }

    private fun handleTableCell(element: Element, blocks: MutableList<ContentBlock>) {
        // 单独的单元格处理（用于非tr包裹的td/th）
        val cellBlocks = parseNodes(element.childNodes(), allowTable = false)
        blocks.add(ContentBlock.TableCellBlock(cellBlocks))
    }

}