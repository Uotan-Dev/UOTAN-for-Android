package com.gustate.uotan.section.data.repository

import com.gustate.uotan.section.data.model.SectionData
import com.gustate.uotan.section.data.parse.SectionDataParse
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