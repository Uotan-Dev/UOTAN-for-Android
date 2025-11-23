package com.gustate.uotan.utils.mode

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class AppModeManager @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val prefs = context.getSharedPreferences(
        "app_mode", Context.MODE_PRIVATE)

    private val _mode = MutableStateFlow(value = getAppMode())
    val mode: StateFlow<AppMode> = _mode.asStateFlow()

    fun setAppMode(newMode: AppMode) {
        prefs.edit { putString("mode", newMode.name) }
        _mode.value = newMode
    }

    private fun getAppMode(): AppMode {
        val saved = prefs.getString("mode", AppMode.NONE.name)
        return AppMode.valueOf(value = saved ?: AppMode.NONE.name)
    }

}