package com.gustate.uotan.section.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.section.data.model.SectionData
import com.gustate.uotan.section.data.model.SectionDataItem
import com.gustate.uotan.section.data.repository.SectionDataRepository
import kotlinx.coroutines.launch

class SectionDataViewModel: ViewModel() {

    private val sectionDataRepository = SectionDataRepository()

    private val _normalSectionDataList = MutableLiveData<MutableList<SectionDataItem>>()
    val normalSectionDataList: MutableLiveData<MutableList<SectionDataItem>> get() = _normalSectionDataList

    private val _topSectionDataList = MutableLiveData<MutableList<SectionDataItem>>()
    val topSectionDataList: MutableLiveData<MutableList<SectionDataItem>> get() = _topSectionDataList

    private val _hasMoreData = MutableLiveData(true)
    val hasMoreData: MutableLiveData<Boolean> get() = _hasMoreData

    private val _sectionDescribe = MutableLiveData<String>()
    val sectionDescribe: MutableLiveData<String> get() = _sectionDescribe

    private lateinit var sectionData: SectionData
    private var currentPage = 1
    private var isInitialLoadDone = false

    fun loadInitialData(
        url: String,
        isRefresh: Boolean,
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        if (isRefresh) {
            currentPage = 1
            isInitialLoadDone = false
        } else {
            if (isInitialLoadDone) {
                onSuccess()
                return
            }
        }
        viewModelScope.launch {
            sectionDataRepository.getSectionDataParse(
                url = url,
                onSuccess = {
                    sectionData = it
                    sectionDescribe.value = sectionData.describe
                    normalSectionDataList.value = it.normalItemsList
                    topSectionDataList.value = it.topItemsList
                    currentPage ++
                    hasMoreData.value = it.totalPage >= currentPage
                    isInitialLoadDone = true
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }

    fun loadMoreData(onSuccess: () -> Unit,onException: (Exception) -> Unit) {
        viewModelScope.launch {
            sectionDataRepository.getSectionDataParse(
                url = sectionData.pageUrl + currentPage,
                onSuccess = {
                    val currentList = normalSectionDataList.value?.toMutableList() ?: mutableListOf()
                    currentList.addAll(it.normalItemsList)
                    normalSectionDataList.value = currentList
                    currentPage ++
                    hasMoreData.value = it.totalPage >= currentPage
                    onSuccess()
                },
                onException = {
                    onException(it)
                }
            )
        }
    }
}