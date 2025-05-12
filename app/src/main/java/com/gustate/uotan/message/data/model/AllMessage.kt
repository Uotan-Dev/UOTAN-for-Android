package com.gustate.uotan.message.data.model

data class AllMessage(
    val userId: String,
    val userName: String,
    val isLikeNotice: Boolean,
    val likeContent: String,
    val isCommentNotice: Boolean,
    val commentContent: String,
    val commentUrl: String,
    val isUpdateNotice: Boolean,
    val updateContent: String,
    val updateUrl: String,
    val isIntegralNotice: Boolean,
    val integralType: String,
    val integralCount: String,
    val isSystemNotice: Boolean,
    val systemContent: String,
    val time: String
)
