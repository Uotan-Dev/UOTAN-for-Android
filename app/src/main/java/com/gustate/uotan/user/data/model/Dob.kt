package com.gustate.uotan.user.data.model


import com.google.gson.annotations.SerializedName

data class Dob(
    @SerializedName("day")
    val day: Int,
    @SerializedName("month")
    val month: Int,
    @SerializedName("year")
    val year: Int
)