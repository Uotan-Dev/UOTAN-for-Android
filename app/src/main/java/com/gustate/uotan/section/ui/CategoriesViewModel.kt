package com.gustate.uotan.section.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.section.data.repository.SectionItemRepository
import kotlinx.coroutines.launch

class CategoriesViewModel: ViewModel() {

    // 实例化网络操作
    private val sectionItemRepository = SectionItemRepository()

    private val _sectionList = MutableLiveData<MutableList<SectionItem>>()
    val sectionList: MutableLiveData<MutableList<SectionItem>> get() = _sectionList

    private var isInitialLoadDone = false

    fun updateSectionList(
        url: String,
        onThrowable: (Throwable) -> Unit
    ) {
        if (isInitialLoadDone) return
        viewModelScope.launch {
            sectionItemRepository.getSectionItemList(
                section = url,
                onSuccess = {
                    isInitialLoadDone = true
                    _sectionList.value = it
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }

}