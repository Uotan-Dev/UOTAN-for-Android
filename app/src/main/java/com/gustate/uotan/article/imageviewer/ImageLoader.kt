package com.gustate.uotan.article.imageviewer

import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.iielse.imageviewer.core.ImageLoader
import com.github.iielse.imageviewer.core.Photo
import com.gustate.uotan.threads.data.model.ThreadPhoto

// 基本是固定写法. Glide 可以换成别的. demo代码中有video的写法.
class ImageLoader : ImageLoader {
    /** 根据自身photo数据加载图片.可以使用其它图片加载框架. */
    override fun load(view: ImageView, data: Photo, viewHolder: RecyclerView.ViewHolder) {
        val it = (data as? ThreadPhoto?)?.url ?: return
        Glide.with(view).load(it)
            .placeholder(view.drawable)
            .into(view)
    }
}