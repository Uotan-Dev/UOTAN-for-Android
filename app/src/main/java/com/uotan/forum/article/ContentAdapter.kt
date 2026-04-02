package com.uotan.forum.article

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.uotan.forum.article.viewholder.HeadingViewHolder
import com.uotan.forum.article.viewholder.IframeViewHolder
import com.uotan.forum.article.viewholder.ImageViewHolder
import com.uotan.forum.article.viewholder.TableViewHolder
import com.uotan.forum.article.viewholder.TextViewHolder
import com.uotan.forum.R

class ContentAdapter(
    private val isReply: Boolean = false
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var blockList = mutableListOf<ContentBlock>()

    private var imageIndex = -1
    var onImageClick: ((position: Int, src: String) -> Unit)? = null

    override fun getItemViewType(position: Int) = when (blockList[position]) {
        is ContentBlock.TextBlock -> 0
        is ContentBlock.TableBlock -> 1
        is ContentBlock.CodeBlock -> 2
        is ContentBlock.ImageBlock -> 3
        is ContentBlock.QuoteBlock -> 4
        is ContentBlock.IframeBlock -> 5
        is ContentBlock.HeadingBlock -> 6
        is ContentBlock.TableRowBlock -> -1
        is ContentBlock.TableCellBlock -> -2
        is ContentBlock.TableListBlock -> -3
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
            is ContentBlock.TextBlock -> (holder as TextViewHolder).bind(block, isReply)
            is ContentBlock.TableBlock -> (holder as TableViewHolder).bind(block)
            is ContentBlock.ImageBlock -> {
                imageIndex ++
                onImageClick?.let { (holder as ImageViewHolder).bind(block, imageIndex, it) }
            }
            is ContentBlock.IframeBlock -> (holder as IframeViewHolder).bind(block)
            is ContentBlock.HeadingBlock -> (holder as HeadingViewHolder).bind(block)
            else -> {

            }
        }
    }

    fun updateContent(newBlockList: MutableList<ContentBlock>) {
        blockList = newBlockList
        notifyItemChanged(blockList.lastIndex, newBlockList.lastIndex)
    }

}