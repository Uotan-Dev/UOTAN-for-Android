package com.uotan.forum.user.data.model


import com.google.gson.annotations.SerializedName

data class CustomFields(
    @SerializedName("Type")
    val type: Any
)