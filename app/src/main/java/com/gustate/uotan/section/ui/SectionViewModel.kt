package com.gustate.uotan.section.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.section.data.model.Categories
import com.gustate.uotan.section.data.model.SectionItem
import com.gustate.uotan.section.data.repository.SectionItemRepository
import kotlinx.coroutines.launch

class SectionViewModel: ViewModel() {
    // 实例化网络操作
    private val sectionItemRepository = SectionItemRepository()
    private val _currentTabPosition = MutableLiveData<Int>()
    val currentTabPosition: MutableLiveData<Int> get() = _currentTabPosition
    private val _followList = MutableLiveData<MutableList<SectionItem>>()
    val followList: MutableLiveData<MutableList<SectionItem>> get() = _followList
    private val _sectionCollapseList = MutableLiveData<MutableList<Categories>>()
    val sectionCollapseList: MutableLiveData<MutableList<Categories>> get() = _sectionCollapseList
    private var isInitialLoadDone = false
    private var isInitialLoadFollowDone = false
    fun updateSectionList(isInitialLoad: Boolean, position: Int, onThrowable: (Throwable) -> Unit) {
        if (isInitialLoad && isInitialLoadDone) return
        if (_currentTabPosition.value == position) return
        _currentTabPosition.value = position
        viewModelScope.launch {
            sectionItemRepository.getCollapseSectionItemList(
                onSuccess = {
                    isInitialLoadDone = true
                    _sectionCollapseList.value = it
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }
    fun updateFollowList(
        onFirstSuccess: (MutableList<SectionItem>) -> Unit,
        onSuccess: () -> Unit,
        onThrowable: (Throwable) -> Unit
    ) {
        if (isInitialLoadFollowDone) {
            onSuccess()
            return
        }
        viewModelScope.launch {
            sectionItemRepository.getSectionItemList(
                section = "/watched/forums",
                onSuccess = {
                    isInitialLoadFollowDone = true
                    _followList.value = it
                    onFirstSuccess(it)
                },
                onThrowable = {
                    onThrowable(it)
                }
            )
        }
    }
    fun setCurrentTabPosition(position: Int) {
        _currentTabPosition.value = position
    }
}