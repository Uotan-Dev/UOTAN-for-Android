package com.uotan.forum.search.data.model

data class SearchItem(
    val topic: String,
    val url: String,
    val title: String,
    val content: String,
    val author: String,
    val id: String,
    val authorUrl: String,
    val type: String,
    val time: String,
    val tag: MutableList<String>,
    val comment: String,
    val plate: String,
    val cover: String
)
