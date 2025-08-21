package com.gustate.uotan.threads.data.model.post


import com.google.gson.annotations.SerializedName

data class PostResponse(
    @SerializedName("post")
    val post: Post,
    @SerializedName("replyPosts")
    val replyPosts: List<ReplyPost>
)