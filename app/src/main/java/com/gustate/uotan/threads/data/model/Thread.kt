package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class Thread(
    @SerializedName("can_edit")
    val canEdit: Boolean,
    @SerializedName("can_edit_tags")
    val canEditTags: Boolean,
    @SerializedName("can_hard_delete")
    val canHardDelete: Boolean,
    @SerializedName("can_reply")
    val canReply: Boolean,
    @SerializedName("can_soft_delete")
    val canSoftDelete: Boolean,
    @SerializedName("can_view_attachments")
    val canViewAttachments: Boolean,
    @SerializedName("custom_fields")
    val customFields: CustomFields,
    @SerializedName("discussion_open")
    val discussionOpen: Boolean,
    @SerializedName("discussion_state")
    val discussionState: String,
    @SerializedName("discussion_type")
    val discussionType: String,
    @SerializedName("first_post_id")
    val firstPostId: Int,
    @SerializedName("first_post_reaction_score")
    val firstPostReactionScore: Int,
    @SerializedName("Forum")
    val forum: Forum,
    @SerializedName("highlighted_post_ids")
    val highlightedPostIds: List<Any?>,
    @SerializedName("is_first_post_pinned")
    val isFirstPostPinned: Boolean,
    @SerializedName("is_unread")
    val isUnread: Boolean,
    @SerializedName("is_watching")
    val isWatching: Boolean,
    @SerializedName("last_post_date")
    val lastPostDate: Int,
    @SerializedName("last_post_id")
    val lastPostId: Int,
    @SerializedName("last_post_user_id")
    val lastPostUserId: Int,
    @SerializedName("last_post_username")
    val lastPostUsername: String,
    @SerializedName("node_id")
    val nodeId: Int,
    @SerializedName("post_date")
    val postDate: Int,
    @SerializedName("prefix")
    val prefix: String,
    @SerializedName("prefix_id")
    val prefixId: Int,
    @SerializedName("reply_count")
    val replyCount: Int,
    @SerializedName("sticky")
    val sticky: Boolean,
    @SerializedName("tags")
    val tags: List<String>,
    @SerializedName("thread_id")
    val threadId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("User")
    val user: UserX,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("username")
    val username: String,
    @SerializedName("view_count")
    val viewCount: Int,
    @SerializedName("view_url")
    val viewUrl: String,
    @SerializedName("visitor_post_count")
    val visitorPostCount: Int
)