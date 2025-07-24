package com.gustate.uotan.threads.data.model

import com.github.iielse.imageviewer.adapter.ItemType
import com.github.iielse.imageviewer.core.Photo

data class ThreadPhoto(
    val url: String,
    val index: Long
) : Photo {
    override fun id(): Long = index
    override fun itemType() = ItemType.PHOTO
}
