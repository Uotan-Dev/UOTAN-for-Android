package com.uotan.forum.section.data.model

data class SectionData(
    val describe: String,
    val totalPage: Int,
    val topItemsList: MutableList<SectionDataItem>,
    val normalItemsList: MutableList<SectionDataItem>,
    val pageUrl: String
)
