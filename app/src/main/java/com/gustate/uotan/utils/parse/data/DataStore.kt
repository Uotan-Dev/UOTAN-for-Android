package com.gustate.uotan.utils.parse.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// 通过扩展属性创建 DataStore（自动生成的文件名为 "cookies_preferences.pb"）
val Context.cookiesDataStore by preferencesDataStore(name = "cookies")

class CookiesManager(private val context: Context) {

    // 保存 Cookies
    suspend fun saveCookies(cookies: Map<String, String>) {
        context.cookiesDataStore.edit { preferences ->
            cookies.forEach { (key, value) ->
                // 使用通用方法处理所有键值对
                preferences[stringPreferencesKey(key)] = value
            }
        }
    }

    // 获取全部 Cookies
    val cookiesFlow: Flow<Map<String, String>> = context.cookiesDataStore.data
        .map { preferences ->
            preferences.asMap().mapKeys { it.key.name }.mapValues { it.value.toString() }
        }

    // 清除 Cookies
    suspend fun clearCookies() {
        context.cookiesDataStore.edit { preferences ->
            preferences.clear()
        }
    }
}