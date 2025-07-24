package com.gustate.uotan.main.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.user.data.model.User
import com.gustate.uotan.user.data.repository.MineRepository
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val mineRepository = MineRepository()

    private val _pagerPage = MutableLiveData(0)
    val pagerPage: MutableLiveData<Int> get() = _pagerPage

    private val _mineUserInfo = MutableLiveData<User>()
    val mineUserInfo: MutableLiveData<User> get() = _mineUserInfo

    private var isInitialLoadDone = false

    fun loadInitial(
        onSuccess: () -> Unit,
        onException: (Exception) -> Unit
    ) {
        if (isInitialLoadDone) return
        _pagerPage.value = 0
        isInitialLoadDone = true
    }

    fun pager(page: Int) {
        _pagerPage.value = page
    }
}