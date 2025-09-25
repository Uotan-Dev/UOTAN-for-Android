package com.gustate.uotan.user.data.model


import com.google.gson.annotations.SerializedName

data class Me(
    @SerializedName("about")
    val about: String,
    @SerializedName("activity_visible")
    val activityVisible: Boolean,
    @SerializedName("age")
    val age: Int,
    @SerializedName("alert_optout")
    val alertOptout: List<Any?>,
    @SerializedName("allow_post_profile")
    val allowPostProfile: String,
    @SerializedName("allow_receive_news_feed")
    val allowReceiveNewsFeed: String,
    @SerializedName("allow_send_personal_conversation")
    val allowSendPersonalConversation: String,
    @SerializedName("allow_view_identities")
    val allowViewIdentities: String,
    @SerializedName("allow_view_profile")
    val allowViewProfile: String,
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
    @SerializedName("content_show_signature")
    val contentShowSignature: Boolean,
    @SerializedName("creation_watch_state")
    val creationWatchState: String,
    @SerializedName("custom_fields")
    val customFields: CustomFields,
    @SerializedName("custom_title")
    val customTitle: String,
    @SerializedName("dob")
    val dob: Dob,
    @SerializedName("email")
    val email: String,
    @SerializedName("email_on_conversation")
    val emailOnConversation: Boolean,
    @SerializedName("gravatar")
    val gravatar: String,
    @SerializedName("interaction_watch_state")
    val interactionWatchState: String,
    @SerializedName("is_admin")
    val isAdmin: Boolean,
    @SerializedName("is_banned")
    val isBanned: Boolean,
    @SerializedName("is_followed")
    val isFollowed: Boolean,
    @SerializedName("is_ignored")
    val isIgnored: Boolean,
    @SerializedName("is_moderator")
    val isModerator: Boolean,
    @SerializedName("is_staff")
    val isStaff: Boolean,
    @SerializedName("is_super_admin")
    val isSuperAdmin: Boolean,
    @SerializedName("last_activity")
    val lastActivity: Int,
    @SerializedName("location")
    val location: String,
    @SerializedName("message_count")
    val messageCount: Int,
    @SerializedName("profile_banner_urls")
    val profileBannerUrls: ProfileBannerUrls,
    @SerializedName("push_on_conversation")
    val pushOnConversation: Boolean,
    @SerializedName("push_optout")
    val pushOptout: List<Any?>,
    @SerializedName("question_solution_count")
    val questionSolutionCount: Int,
    @SerializedName("reaction_score")
    val reactionScore: Int,
    @SerializedName("receive_admin_email")
    val receiveAdminEmail: Boolean,
    @SerializedName("register_date")
    val registerDate: Int,
    @SerializedName("show_dob_date")
    val showDobDate: Boolean,
    @SerializedName("show_dob_year")
    val showDobYear: Boolean,
    @SerializedName("signature")
    val signature: String,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("trophy_points")
    val trophyPoints: Int,
    @SerializedName("usa_tfa")
    val usaTfa: Boolean,
    @SerializedName("use_tfa")
    val useTfa: Boolean,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_title")
    val userTitle: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("view_url")
    val viewUrl: String,
    @SerializedName("visible")
    val visible: Boolean,
    @SerializedName("vote_score")
    val voteScore: Int,
    @SerializedName("warning_points")
    val warningPoints: Int,
    @SerializedName("website")
    val website: String
)