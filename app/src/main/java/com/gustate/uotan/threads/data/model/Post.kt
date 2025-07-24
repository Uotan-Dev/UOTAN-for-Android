package com.gustate.uotan.threads.data.model

import com.google.gson.annotations.SerializedName
import com.gustate.uotan.utils.data.model.Attachment
import com.gustate.uotan.user.data.model.User

data class Post (
    @SerializedName("attach_count")
    val attachCount: Long,
    @SerializedName("Attachments")
    val attachments: List<Attachment>? = null,
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
    @SerializedName("last_edit_date")
    val lastEditDate: Long,
    val message: String,
    @SerializedName("message_parsed")
    val messageParsed: String,
    @SerializedName("message_state")
    val messageState: String,
    val position: Long,
    @SerializedName("post_date")
    val postDate: Long,
    @SerializedName("post_id")
    val postID: Long,
    @SerializedName("reaction_score")
    val reactionScore: Long,
    @SerializedName("thread_id")
    val threadID: Long,
    @SerializedName("User")
    val user: User,
    @SerializedName("user_id")
    val userID: Long,
    val username: String,
    @SerializedName("view_url")
    val viewURL: String,
    @SerializedName("warning_message")
    val warningMessage: String
)
