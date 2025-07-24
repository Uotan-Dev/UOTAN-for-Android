package com.gustate.uotan.article.viewholder

import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.article.ContentBlock.TextBlock
import com.gustate.uotan.R

class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val textView = itemView.findViewById<TextView>(R.id.tv_content)

    fun bind(block: TextBlock) {
        textView.text = HtmlCompat.fromHtml(block.html, HtmlCompat.FROM_HTML_MODE_COMPACT)
    }
}