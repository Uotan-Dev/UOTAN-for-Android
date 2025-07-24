package com.gustate.uotan.settings.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingModel(
    @PrimaryKey @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value") val value: String,
    @ColumnInfo(name = "type") val type: String
) {
    companion object {
        const val DARK_MODE_ENABLED_KEY = "dark_mode_enabled"
        const val DYNAMIC_MONET_ENABLED_KEY = "dynamic_monet_enabled"
        const val DOMAIN_CUSTOM_ENABLED_KEY = "domain_custom_enabled"
        const val DOMAIN_CUSTOM_VALUE_KEY = "domain_custom_value"
        const val SSL_AUTH_DISABLE_KEY = "ssl_auth_disable"
        const val LANGUAGE = "language"
    }
}