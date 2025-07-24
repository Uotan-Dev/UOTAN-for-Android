package com.gustate.uotan.settings.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_ENABLED_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_VALUE_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY
import com.gustate.uotan.settings.data.SettingsRepository
import kotlinx.coroutines.launch

class DomainViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsRepository = SettingsRepository(application)

    private val _isDomainCustom = MutableLiveData<Boolean>()
    private val _customDomainValue = MutableLiveData<String>()
    private val _isSslAuthDisable = MutableLiveData<Boolean>()

    val isDomainCustom: MutableLiveData<Boolean> get() = _isDomainCustom
    val customDomainValue: MutableLiveData<String> get() = _customDomainValue
    val isSslAuthDisable: MutableLiveData<Boolean> get() = _isSslAuthDisable

    init {
        viewModelScope.launch {
            _isDomainCustom.value =
                settingsRepository.getSettingByKey(DOMAIN_CUSTOM_ENABLED_KEY).value.toBoolean()
            _customDomainValue.value =
                settingsRepository.getSettingByKey(DOMAIN_CUSTOM_VALUE_KEY).value
            _isSslAuthDisable.value =
                settingsRepository.getSettingByKey(SSL_AUTH_DISABLE_KEY).value.toBoolean()
        }
    }

    fun updateSettingValue(key:String, value: String) {
        viewModelScope.launch {
            settingsRepository.updateSettingValue(key, value)
            when (key) {
                DOMAIN_CUSTOM_ENABLED_KEY -> _isDomainCustom.value = value.toBoolean()
                DOMAIN_CUSTOM_VALUE_KEY -> _customDomainValue.value = value
                SSL_AUTH_DISABLE_KEY -> _isSslAuthDisable.value = value.toBoolean()
            }
        }
    }
}