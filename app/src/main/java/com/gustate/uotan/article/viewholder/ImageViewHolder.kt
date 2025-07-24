package com.gustate.uotan.article.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gustate.uotan.article.ContentBlock.ImageBlock
import com.gustate.uotan.R
import com.gustate.uotan.article.imageviewer.ImageTransformer

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val imgContent = itemView.findViewById<ImageView>(R.id.img_content)
    private val tvAlt = itemView.findViewById<TextView>(R.id.tv_alt)

    fun bind(block: ImageBlock, imgIndex: Int, onImageClick: (position: Int, src: String) -> Unit) {
        Glide.with(imgContent.context).load(block.src).into(imgContent)
        tvAlt.isGone = block.alt.isEmpty()
        tvAlt.text = block.alt
        imgContent.setOnClickListener {
            ImageTransformer.put(imgIndex.toLong(), imgContent)
            onImageClick(imgIndex, block.src)
        }
    }
}