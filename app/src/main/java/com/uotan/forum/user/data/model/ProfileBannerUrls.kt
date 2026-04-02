package com.uotan.forum.user.data.model


import com.google.gson.annotations.SerializedName

data class ProfileBannerUrls(
    @SerializedName("l")
    val l: String,
    @SerializedName("m")
    val m: String
)