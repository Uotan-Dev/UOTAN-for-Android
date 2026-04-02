package com.uotan.forum.resource.data.model

data class ResReplyData(
    val authorId: String,
    val author: String,
    val rating: Float,
    val time: String,
    val version: String,
    val content: String
)