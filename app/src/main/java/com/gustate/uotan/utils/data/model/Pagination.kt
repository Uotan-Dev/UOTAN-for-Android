package com.gustate.uotan.utils.data.model

import com.google.gson.annotations.SerializedName

data class Pagination (
    @SerializedName("current_page")
    val currentPage: Long,
    @SerializedName("last_page")
    val lastPage: Long,
    @SerializedName("per_page")
    val perPage: Long,
    val shown: Long,
    val total: Long
)