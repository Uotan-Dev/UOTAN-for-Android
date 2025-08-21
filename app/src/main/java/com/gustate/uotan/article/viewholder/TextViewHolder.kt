package com.gustate.uotan.article.viewholder

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.gustate.uotan.article.ContentBlock.TextBlock
import com.gustate.uotan.R
import com.gustate.uotan.utils.Utils.Companion.dpToPx
import kotlin.math.roundToInt

class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val textView = itemView.findViewById<TextView>(R.id.tv_content)

    fun bind(block: TextBlock, isReply: Boolean = false) {
        textView.textSize = if (isReply) 14f else 16f
        textView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin = if (isReply) 0 else 12f.dpToPx(itemView.context).roundToInt()
        }
        textView.movementMethod = LinkMovementMethod.getInstance()
        textView.text = block.html
    }
}