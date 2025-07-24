package com.gustate.uotan.section.data.repository

import com.gustate.uotan.section.data.model.Categories
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.section.data.parse.SectionItemParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SectionItemRepository {
    private val sectionItemParse = SectionItemParse()

    suspend fun getCollapseSectionItemList(
        onSuccess: (MutableList<Categories>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        sectionItemParse.parseCollapseSectionItemList(onSuccess, onThrowable)
    }

    suspend fun getSectionItemList(
        section: String,
        onSuccess: (MutableList<SectionItem>) -> Unit,
        onThrowable: (Throwable) -> Unit
    ) = withContext(Dispatchers.IO) {
        sectionItemParse.parseCategoriesSectionList(section, onSuccess, onThrowable)
    }
}