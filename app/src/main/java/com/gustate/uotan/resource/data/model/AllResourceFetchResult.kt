package com.gustate.uotan.resource.data.model

class AllResourceFetchResult(
    val items: MutableList<ResourceItem>,
    val totalPage: Int,
    val nextPageUrl: String
)