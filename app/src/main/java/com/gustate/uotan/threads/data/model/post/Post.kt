package com.gustate.uotan.threads.data.model.post

import com.google.gson.annotations.SerializedName

data class Post(
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
    @SerializedName("is_first_post")
    val isFirstPost: Boolean,
    @SerializedName("is_last_post")
    val isLastPost: Boolean,
    @SerializedName("is_reacted_to")
    val isReactedTo: Boolean,
    @SerializedName("is_unread")
    val isUnread: Boolean,
    @SerializedName("last_edit_date")
    val lastEditDate: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("message_parsed")
    val messageParsed: String,
    @SerializedName("message_state")
    val messageState: String,
    @SerializedName("position")
    val position: Int,
    @SerializedName("post_date")
    val postDate: Int,
    @SerializedName("post_id")
    val postId: Int,
    @SerializedName("reaction_score")
    val reactionScore: Int,
    @SerializedName("Thread")
    val thread: Thread,
    @SerializedName("thread_id")
    val threadId: Int,
    @SerializedName("User")
    val user: UserX,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("view_url")
    val viewUrl: String,
    @SerializedName("warning_message")
    val warningMessage: String
)