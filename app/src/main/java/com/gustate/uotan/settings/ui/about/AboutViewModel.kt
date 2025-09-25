package com.gustate.uotan.settings.ui.about

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AboutViewModel: ViewModel() {

    private var _scrollProgress = MutableLiveData(0f)
    val scrollProgress get() = _scrollProgress

    fun updateScrollProgress(progress: Float) {
        scrollProgress.value = progress
    }

}