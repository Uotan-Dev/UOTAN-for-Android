package com.uotan.forum.resource.data.model

data class PurchaseData(
    val resType: ResourceType,
    val isPaid: Boolean,
    val driveName: String,
    val code: String,
    val price: String,
    val url: String
)