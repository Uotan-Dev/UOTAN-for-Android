package com.gustate.uotan.home.data.model

data class Recommend(
    val items: MutableList<RecommendItem>,
    val totalPage: Int
)
