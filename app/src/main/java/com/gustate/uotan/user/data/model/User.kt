package com.gustate.uotan.user.data.model

import com.google.gson.annotations.SerializedName

data class User (
    @SerializedName("andrew_user_note_count")
    val andrewUserNoteCount: Long,
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
    @SerializedName("is_followed")
    val isFollowed: Boolean,
    @SerializedName("is_staff")
    val isStaff: Boolean,
    @SerializedName("last_activity")
    val lastActivity: Long,
    val location: String,
    @SerializedName("message_count")
    val messageCount: Long,
    @SerializedName("profile_banner_urls")
    val profileBannerUrls: ProfileBannerUrls,
    @SerializedName("question_solution_count")
    val questionSolutionCount: Long,
    @SerializedName("reaction_score")
    val reactionScore: Long,
    @SerializedName("register_date")
    val registerDate: Long,
    val signature: String,
    @SerializedName("trophy_points")
    val trophyPoints: Long,
    @SerializedName("user_id")
    val userID: Long,
    @SerializedName("user_title")
    val userTitle: String,
    val username: String,
    @SerializedName("view_url")
    val viewURL: String,
    @SerializedName("vote_score")
    val voteScore: Long
)