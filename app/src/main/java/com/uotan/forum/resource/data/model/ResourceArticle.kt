package com.uotan.forum.resource.data.model

data class ResourceArticle (
    val title: String,
    val cover: String,
    val device: String,
    val downloadType: String,
    val size: String,
    val password: String,
    val author: String,
    val authorUrl: String,
    val authorId: String,
    val authorAvatar: String,
    val downloadCount: String,
    val viewCount: String,
    val firstPost: String,
    val latestPost: String,
    val downloadUrl: String,
    val content: String,
    val numberOfLikes: String,
    val reactUrl: String,
    val isReact: Boolean,
    val isBookMark: Boolean
)