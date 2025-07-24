package com.gustate.uotan.article.imageviewer

import android.view.View
import android.widget.ImageView
import com.github.iielse.imageviewer.core.Transformer

class ImageTransformer : Transformer {
    override fun getView(key: Long): ImageView? = provide(key)

    companion object {
        private val transition = HashMap<ImageView, Long>()
        fun put(photoId: Long, imageView: ImageView) {
            if (!imageView.isAttachedToWindow) return
            imageView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) = Unit
                override fun onViewDetachedFromWindow(v: View) {
                    transition.remove(imageView)
                    imageView.removeOnAttachStateChangeListener(this)
                }
            })
            transition[imageView] = photoId
        }

        private fun provide(photoId: Long): ImageView? {
            transition.keys.forEach {
                if (transition[it] == photoId)
                    return it
            }
            return null
        }
    }
}