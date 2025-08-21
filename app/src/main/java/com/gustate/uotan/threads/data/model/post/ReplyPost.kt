package com.gustate.uotan.threads.data.model.post


import com.google.gson.annotations.SerializedName

data class ReplyPost(
    @SerializedName("attach_count")
    val attachCount: Int,
    @SerializedName("can_edit")
    val canEdit: Boolean,
    @SerializedName("can_hard_delete")
    val canHardDelete: Boolean,
    @SerializedName("can_react")
    val canReact: Boolean,
    @SerializedName("can_soft_delete")
    val canSoftDelete: Boolean,
    @SerializedName("can_view_attachments")
    val canViewAttachments: Boolean,
    @SerializedName("is_reacted_to")
    val isReactedTo: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("message_parsed")
    val messageParsed: String,
    @SerializedName("post_id")
    val postId: Int,
    @SerializedName("reaction_score")
    val reactionScore: Int,
    @SerializedName("reply_post_id")
    val replyPostId: Int,
    @SerializedName("sticky")
    val sticky: Boolean,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("view_url")
    val viewUrl: String,
    @SerializedName("warning_message")
    val warningMessage: String
)