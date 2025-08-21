package com.gustate.uotan.resource.data.parse

class ResourceData {

    sealed class ResourceType {
        object Old : ResourceType()
        object New : ResourceType()
        object Other : ResourceType()
    }

    data class PurchaseData(
        val resType: ResourceType,
        val isPaid: Boolean,
        val driveName: String,
        val code: String,
        val price: String,
        val url: String
    )

}