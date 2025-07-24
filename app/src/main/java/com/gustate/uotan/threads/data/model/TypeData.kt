package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class TypeData(
    @SerializedName("allow_posting")
    val allowPosting: Boolean,
    @SerializedName("can_create_thread")
    val canCreateThread: Boolean,
    @SerializedName("can_upload_attachment")
    val canUploadAttachment: Boolean,
    @SerializedName("discussion")
    val discussion: Discussion,
    @SerializedName("discussion_count")
    val discussionCount: Int,
    @SerializedName("forum_type_id")
    val forumTypeId: String,
    @SerializedName("is_unread")
    val isUnread: Boolean,
    @SerializedName("last_post_date")
    val lastPostDate: Int,
    @SerializedName("last_post_id")
    val lastPostId: Int,
    @SerializedName("last_post_username")
    val lastPostUsername: String,
    @SerializedName("last_thread_id")
    val lastThreadId: Int,
    @SerializedName("last_thread_prefix_id")
    val lastThreadPrefixId: Int,
    @SerializedName("last_thread_title")
    val lastThreadTitle: String,
    @SerializedName("message_count")
    val messageCount: Int,
    @SerializedName("min_tags")
    val minTags: Int,
    @SerializedName("require_prefix")
    val requirePrefix: Boolean
)