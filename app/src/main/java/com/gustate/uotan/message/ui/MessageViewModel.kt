package com.gustate.uotan.message.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MessageViewModel: ViewModel() {

    private val _currentPage = MutableLiveData(0)
    val currentPage: MutableLiveData<Int> get() = _currentPage

    fun onPageSelected(page: Int) {
        _currentPage.value = page
    }

}