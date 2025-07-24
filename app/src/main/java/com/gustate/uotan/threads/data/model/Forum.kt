package com.gustate.uotan.threads.data.model


import com.google.gson.annotations.SerializedName

data class Forum(
    @SerializedName("breadcrumbs")
    val breadcrumbs: List<Breadcrumb>,
    @SerializedName("description")
    val description: String,
    @SerializedName("display_in_list")
    val displayInList: Boolean,
    @SerializedName("display_order")
    val displayOrder: Int,
    @SerializedName("node_id")
    val nodeId: Int,
    @SerializedName("node_name")
    val nodeName: Any,
    @SerializedName("node_type_id")
    val nodeTypeId: String,
    @SerializedName("parent_node_id")
    val parentNodeId: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("type_data")
    val typeData: TypeData,
    @SerializedName("view_url")
    val viewUrl: String
)