package com.gustate.uotan.article.viewholder

import android.graphics.Typeface.DEFAULT_BOLD
import android.view.View
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.R
import com.gustate.uotan.article.ContentBlock.HeadingBlock

class HeadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val textView = itemView.findViewById<TextView>(R.id.tv_content)

    fun bind(block: HeadingBlock) {
        textView.text = HtmlCompat.fromHtml(block.text, HtmlCompat.FROM_HTML_MODE_COMPACT)
        textView.textSize = when(block.level) {
            1 -> 20f
            2 -> 19f
            3 -> 18f
            4 -> 17f
            5 -> 16f
            else -> 16f
        }
        when(block.level) {
            1, 2, 3, 4 -> textView.setTypeface(DEFAULT_BOLD)
        }
    }
}