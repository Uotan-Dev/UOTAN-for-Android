package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class CustomFields(
    @SerializedName("fxts")
    val fxts: String,
    @SerializedName("jingtie")
    val jingtie: String,
    @SerializedName("zhiding")
    val zhiding: String
)