package com.uotan.forum.section.data.repository

import com.uotan.forum.section.data.model.SectionData
import com.uotan.forum.section.data.parse.SectionDataParse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SectionDataRepository {
    private val sectionDataParse = SectionDataParse()
    suspend fun getSectionDataParse(
        url: String,
        onSuccess: (SectionData) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        sectionDataParse.parseSectionData(url, onSuccess, onException)
    }
}