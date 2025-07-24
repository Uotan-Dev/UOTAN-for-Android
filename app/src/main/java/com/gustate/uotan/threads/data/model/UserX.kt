package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class UserX(
    @SerializedName("andrew_user_note_count")
    val andrewUserNoteCount: Int,
    @SerializedName("avatar_urls")
    val avatarUrls: AvatarUrls,
    @SerializedName("can_ban")
    val canBan: Boolean,
    @SerializedName("can_converse")
    val canConverse: Boolean,
    @SerializedName("can_edit")
    val canEdit: Boolean,
    @SerializedName("can_follow")
    val canFollow: Boolean,
    @SerializedName("can_ignore")
    val canIgnore: Boolean,
    @SerializedName("can_post_profile")
    val canPostProfile: Boolean,
    @SerializedName("can_view_profile")
    val canViewProfile: Boolean,
    @SerializedName("can_view_profile_posts")
    val canViewProfilePosts: Boolean,
    @SerializedName("can_warn")
    val canWarn: Boolean,
    @SerializedName("custom_fields")
    val customFields: CustomFieldsX,
    @SerializedName("is_banned")
    val isBanned: Boolean,
    @SerializedName("is_followed")
    val isFollowed: Boolean,
    @SerializedName("is_ignored")
    val isIgnored: Boolean,
    @SerializedName("is_staff")
    val isStaff: Boolean,
    @SerializedName("last_activity")
    val lastActivity: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("message_count")
    val messageCount: Int,
    @SerializedName("profile_banner_urls")
    val profileBannerUrls: ProfileBannerUrls,
    @SerializedName("question_solution_count")
    val questionSolutionCount: Int,
    @SerializedName("reaction_score")
    val reactionScore: Int,
    @SerializedName("register_date")
    val registerDate: Int,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("trophy_points")
    val trophyPoints: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_title")
    val userTitle: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("view_url")
    val viewUrl: String,
    @SerializedName("vote_score")
    val voteScore: Int,
    @SerializedName("warning_points")
    val warningPoints: Int,
    @SerializedName("website")
    val website: String
)