package com.gustate.uotan.user.data.model


import com.google.gson.annotations.SerializedName

data class ProfileBannerUrls(
    @SerializedName("l")
    val l: String,
    @SerializedName("m")
    val m: String
)