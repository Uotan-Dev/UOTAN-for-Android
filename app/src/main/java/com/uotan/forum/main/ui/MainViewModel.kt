package com.uotan.forum.main.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.uotan.forum.user.data.model.MeModel
import com.uotan.forum.user.data.repository.MineRepository
import com.uotan.forum.utils.mode.AppModeManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    appModeManager: AppModeManager
) : ViewModel() {

    val appMode = appModeManager.mode.asLiveData()

    private val mineRepository = MineRepository()

    private val _pagerPage = MutableLiveData(0)
    val pagerPage: MutableLiveData<Int> get() = _pagerPage

    private val _mineUserInfo = MutableLiveData<MeModel>()
    val mineUserInfo: MutableLiveData<MeModel> get() = _mineUserInfo

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