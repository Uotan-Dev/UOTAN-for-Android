package com.gustate.uotan.section.data.repository

import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.section.data.parse.SectionItemParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SectionItemRepository {
    private val sectionItemParse = SectionItemParse()
    suspend fun getSectionItemList(
        section: String,
        onSuccess: (MutableList<SectionItem>) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        sectionItemParse.parseSectionItemList(section, onSuccess, onException)
    }
}