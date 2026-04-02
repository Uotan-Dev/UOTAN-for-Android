package com.uotan.forum.threads.data.model

data class NoApiPostInfo(
    val title: String,
    val isBookMark: Boolean,
    val ipAddress: String,
    val isJingTie: Boolean,
    val isLocked: Boolean
)