package com.gustate.uotan.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import com.gustate.uotan.article.ContentBlock.CodeBlock
import com.gustate.uotan.article.ContentBlock.HeadingBlock
import com.gustate.uotan.article.ContentBlock.IframeBlock
import com.gustate.uotan.article.ContentBlock.ImageBlock
import com.gustate.uotan.article.ContentBlock.QuoteBlock
import com.gustate.uotan.article.ContentBlock.TableBlock
import com.gustate.uotan.article.ContentBlock.TableCellBlock
import com.gustate.uotan.article.ContentBlock.TableListBlock
import com.gustate.uotan.article.ContentBlock.TableRowBlock
import com.gustate.uotan.article.ContentBlock.TextBlock
import com.gustate.uotan.article.viewholder.HeadingViewHolder
import com.gustate.uotan.article.viewholder.IframeViewHolder
import com.gustate.uotan.article.viewholder.ImageViewHolder
import com.gustate.uotan.article.viewholder.TableViewHolder
import com.gustate.uotan.article.viewholder.TextViewHolder

class ContentAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var blockList = mutableListOf<ContentBlock>()

    private var imageIndex = -1
    var onImageClick: ((position: Int, src: String) -> Unit)? = null

    override fun getItemViewType(position: Int) = when (blockList[position]) {
        is TextBlock -> 0
        is TableBlock -> 1
        is CodeBlock -> 2
        is ImageBlock -> 3
        is QuoteBlock -> 4
        is IframeBlock -> 5
        is HeadingBlock -> 6
        is TableRowBlock -> -1
        is TableCellBlock -> -2
        is TableListBlock -> -3
    }

    override fun getItemCount() = blockList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> TextViewHolder(inflater.inflate(R.layout.article_text_block, parent, false))
            1 -> TableViewHolder(inflater.inflate(R.layout.article_table_block, parent, false))
            3 -> ImageViewHolder(inflater.inflate(R.layout.article_image_block, parent, false))
            5 -> IframeViewHolder(inflater.inflate(R.layout.article_iframe_block, parent, false))
            6 -> HeadingViewHolder(inflater.inflate(R.layout.article_text_block, parent, false))
            else -> TextViewHolder(inflater.inflate(R.layout.article_text_block, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val block = blockList[position]) {
            is TextBlock -> (holder as TextViewHolder).bind(block)
            is TableBlock -> (holder as TableViewHolder).bind(block)
            is ImageBlock -> {
                imageIndex ++
                onImageClick?.let { (holder as ImageViewHolder).bind(block, imageIndex, it) }
            }
            is IframeBlock -> (holder as IframeViewHolder).bind(block)
            is HeadingBlock -> (holder as HeadingViewHolder).bind(block)
            else -> {

            }
        }
    }

    fun updateContent(newBlockList: MutableList<ContentBlock>) {
        blockList = newBlockList
        notifyItemChanged(blockList.lastIndex, newBlockList.lastIndex)
    }

}