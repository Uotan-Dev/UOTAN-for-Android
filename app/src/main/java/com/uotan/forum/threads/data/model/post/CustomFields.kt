package com.uotan.forum.threads.data.model.post


import com.google.gson.annotations.SerializedName

data class CustomFields(
    @SerializedName("fxts")
    val fxts: String,
    @SerializedName("jingtie")
    val jingtie: String,
    @SerializedName("zhiding")
    val zhiding: String
)