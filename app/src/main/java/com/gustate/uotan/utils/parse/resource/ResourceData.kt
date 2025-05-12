package com.gustate.uotan.utils.parse.resource

class ResourceData {

    sealed class ResourceType {
        object Old : ResourceType()
        object New : ResourceType()
        object Other : ResourceType()
    }

    data class ResourceArticle (
        val title: String,
        val cover: String,
        val device: String,
        val downloadType: String,
        val size: String,
        val password: String,
        val author: String,
        val authorUrl: String,
        val authorId: String,
        val authorAvatar: String,
        val downloadCount: String,
        val viewCount: String,
        val firstPost: String,
        val latestPost: String,
        val downloadUrl: String,
        val content: String,
        val numberOfLikes: String,
        val reactUrl: String,
        val isReact: Boolean,
        val isBookMark: Boolean,
        val bookMarkUrl: String
    )

    data class PurchaseData(
        val resType: ResourceType,
        val isPaid: Boolean,
        val driveName: String,
        val code: String,
        val price: String,
        val url: String
    )

    data class ResReportData(
        val authorId: String,
        val author: String,
        val rating: Float,
        val time: String,
        val version: String,
        val content: String
    )

}