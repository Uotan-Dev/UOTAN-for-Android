package com.gustate.uotan.article

import com.gustate.uotan.article.ContentBlock.*
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

        //行内标签集合
        val inlineTags = setOf("b", "strong", "i", "em", "span", "a", "br")

        fun flushText() {
            if (currentText.isNotEmpty()) {
                blocks.add(TextBlock(currentText.toString().trim()))
                currentText.clear()
            }
        }
        for (node in nodes) {
            when (node) {
                is Element -> {
                    if (node.tagName() in inlineTags) {

                    }
                    flushText() // 先处理累积的文本
                    when (node.tagName()) {
                        "a" -> handleLink(node, blocks)
                        "img" -> handleImage(node, blocks)
                        "span" -> handleMediaEmbed(node, blocks)
                        "iframe" -> handleIframe(node, blocks)
                        "br" -> {} // 忽略换行标签
                        "div", "p" -> blocks.addAll(parseNodes(node.childNodes())) // 递归处理容器
                        "ul" -> {
                            // 处理无序列表
                            val listItems = node.getElementsByTag("li")
                            for (item in listItems) {
                                val itemBlocks = parseNodes(item.childNodes())

                                val textContent = itemBlocks.joinToString(" ") {
                                    when(it) {
                                        is TextBlock -> it.html
                                        else -> "<br>此内容嵌套层数过多，请前往网页端查看"
                                    }
                                }
                                blocks.add(TextBlock("· $textContent")) // 无序列表项加圆点前缀
                            }
                        }
                        "ol" -> {
                            // 处理有序列表
                            val listItems = node.getElementsByTag("li")
                            var counter = 1
                            for (item in listItems) {
                                val itemBlocks = parseNodes(item.childNodes(), false)
                                val textContent = itemBlocks.joinToString("") {
                                    when(it) {
                                        is TextBlock -> it.html
                                        else -> "<br>此内容嵌套层数过多，请前往网页端查看"
                                    }
                                }
                                blocks.add(TextBlock("${counter}. $textContent")) // 有序列表项加数字前缀
                                counter++
                            }
                        }
                        "li" -> {
                            // 直接处理孤立的 li 标签
                            flushText()
                            blocks.addAll(parseNodes(node.childNodes(), false))
                        }
                        "h1", "h2", "h3", "h4", "h5", "h6" -> handleHeading(node, blocks)
                        "table" -> if (allowTable) handleTable(node, blocks) else node.html()
                        "tr" -> if (allowTable) handleTableRow(node, blocks) else node.html()
                        "td", "th" -> if (allowTable) handleTableCell(node, blocks) else node.html()
                        else -> {
                            // 对于其他标签，提取其文本内容
                            val text = node.html()
                            if (text.isNotEmpty()) {
                                blocks.add(TextBlock(text))
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
        flushText() // 处理最后剩余的文本
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
            blocks.add(HeadingBlock(level, text))
        }
    }

    private fun handleLink(element: Element, blocks: MutableList<ContentBlock>) {
        // 处理包含图片的链接
        val images = element.select("img")
        if (images.isNotEmpty()) {
            images.forEach { img ->
                val src = img.attr("src")
                val alt = img.attr("alt")
                if (src.isNotBlank()) {
                    blocks.add(ImageBlock(src, alt))
                }
            }
        } else {
            // 处理普通文本链接
            val text = element.text().trim()
            if (text.isNotEmpty()) {
                blocks.add(TextBlock("<a href=\"${element.attr("href")}\">$text</a>"))
            }
        }
    }

    private fun handleImage(element: Element, blocks: MutableList<ContentBlock>) {
        val src = element.attr("src")
        val alt = element.attr("alt")
        if (src.isNotBlank()) {
            blocks.add(ImageBlock(src, alt))
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
                    blocks.add(IframeBlock(src))
                }
            }
        } else {
            // 普通span当作文本处理
            val text = element.text().trim()
            if (text.isNotEmpty()) {
                blocks.add(TextBlock(text))
            }
        }
    }

    private fun handleIframe(element: Element, blocks: MutableList<ContentBlock>) {
        val src = element.attr("src")
        if (src.isNotBlank()) {
            blocks.add(IframeBlock(src))
        }
    }

    private fun handleTable(element: Element, blocks: MutableList<ContentBlock>) {
        val table = TableBlock(mutableListOf())
        element.select("tr").forEach { tr ->
            val row = TableRowBlock(mutableListOf())
            tr.select("td, th").forEach { cell ->
                val cellBlocks = parseNodes(cell.childNodes(), allowTable = false)
                row.cells.add(TableCellBlock(cellBlocks))
            }
            table.rows.add(row)
        }
        blocks.add(table)
    }

    private fun handleTableRow(element: Element, blocks: MutableList<ContentBlock>) {
        // 单独的行处理（用于非table包裹的tr）
        val row = TableRowBlock(mutableListOf())
        element.select("td, th").forEach { cell ->
            val cellBlocks = parseNodes(cell.childNodes(), allowTable = false)
            row.cells.add(TableCellBlock(cellBlocks))
        }
        blocks.add(row)
    }

    private fun handleTableCell(element: Element, blocks: MutableList<ContentBlock>) {
        // 单独的单元格处理（用于非tr包裹的td/th）
        val cellBlocks = parseNodes(element.childNodes(), allowTable = false)
        blocks.add(TableCellBlock(cellBlocks))
    }

}