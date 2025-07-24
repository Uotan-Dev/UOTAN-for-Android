package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class Breadcrumb(
    @SerializedName("node_id")
    val nodeId: Int,
    @SerializedName("node_type_id")
    val nodeTypeId: String,
    @SerializedName("title")
    val title: String
)