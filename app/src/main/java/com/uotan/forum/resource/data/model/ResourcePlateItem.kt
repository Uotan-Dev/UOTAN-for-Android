package com.uotan.forum.resource.data.model

data class ResourcePlateItem(
    val plate: String,
    val plateUrl: String,
    val item: MutableList<String>,
    val itemUrl: MutableList<String>
)