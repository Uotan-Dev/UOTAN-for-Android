package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class AvatarUrls(
    @SerializedName("h")
    val h: String,
    @SerializedName("l")
    val l: String,
    @SerializedName("m")
    val m: String,
    @SerializedName("o")
    val o: String,
    @SerializedName("s")
    val s: String
)