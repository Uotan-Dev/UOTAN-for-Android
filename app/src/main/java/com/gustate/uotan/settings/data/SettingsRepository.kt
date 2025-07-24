package com.gustate.uotan.settings.data

import android.content.Context
import com.gustate.uotan.settings.data.SettingModel.Companion.DARK_MODE_ENABLED_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_ENABLED_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.DOMAIN_CUSTOM_VALUE_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.DYNAMIC_MONET_ENABLED_KEY
import com.gustate.uotan.settings.data.SettingModel.Companion.LANGUAGE
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY

class SettingsRepository(context: Context) {
    val defaultSettingModels = listOf(
        // 个性化
        SettingModel(DARK_MODE_ENABLED_KEY, "false", "BOOL"),
        SettingModel(DYNAMIC_MONET_ENABLED_KEY, "true", "BOOL"),
        // 域名与代理
        SettingModel(DOMAIN_CUSTOM_ENABLED_KEY, "false", "BOOL"),
        SettingModel(DOMAIN_CUSTOM_VALUE_KEY, "www.uotan.cn", "STRING"),
        SettingModel(SSL_AUTH_DISABLE_KEY, "false", "BOOL"),
        SettingModel(LANGUAGE, "system", "STRING")
    )

    val settingsDao = SettingsDatabase.getDatabase(context).settingsDao()

    suspend fun getAllSettings() = settingsDao.getAllSettings()

    suspend fun getSettingByKey(key: String): SettingModel {
        val setting = settingsDao.getSettingByKey(key)
        return if (setting != null)
            setting
        else {
            var defaultSetting = SettingModel("", "", "")
            defaultSettingModels.forEach {
                if (it.key == key) {
                    settingsDao.insertSetting(it)
                    defaultSetting = it
                }
            }
            defaultSetting
        }
    }

    suspend fun updateSettingValue(key: String, value: String) = settingsDao.updateSettingValue(key, value)

}