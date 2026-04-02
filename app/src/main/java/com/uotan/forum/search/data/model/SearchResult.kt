package com.uotan.forum.search.data.model

data class SearchResult(
    val items: MutableList<SearchItem>,
    val totalPage: Int,
    val nextPageUrl: String
)
