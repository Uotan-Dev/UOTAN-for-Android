package com.uotan.forum.message.data.model

data class LikeMessage(
    val userId: String,
    val userName: String,
    val content: String,
    val time: String
)