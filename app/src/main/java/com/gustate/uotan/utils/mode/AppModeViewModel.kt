package com.gustate.uotan.utils.mode

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppModeViewModel @Inject constructor(
    private val appModeManager: AppModeManager
): ViewModel() {

    val appMode = appModeManager.mode

    fun updateMode(newMode: AppMode) {
        appModeManager.setAppMode(newMode)
    }

}