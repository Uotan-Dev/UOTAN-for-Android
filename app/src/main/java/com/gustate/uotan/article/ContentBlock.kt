package com.gustate.uotan.article

sealed class ContentBlock {
    data class TextBlock(val html: String) : ContentBlock()
    data class TableListBlock(val rows: List<List<String>>) : ContentBlock()
    data class CodeBlock(val code: String, val lang: String? = null) : ContentBlock()
    data class ImageBlock(val src: String, val alt: String) : ContentBlock()
    data class QuoteBlock(val nestedBlocks: List<ContentBlock>) : ContentBlock()
    data class IframeBlock(val url: String) : ContentBlock()
    data class HeadingBlock(val level: Int, val text: String) : ContentBlock()
    data class TableBlock(val rows: MutableList<TableRowBlock>) : ContentBlock()
    data class TableRowBlock(val cells: MutableList<TableCellBlock>) : ContentBlock()
    data class TableCellBlock(val blocks: List<ContentBlock>) : ContentBlock()
}
