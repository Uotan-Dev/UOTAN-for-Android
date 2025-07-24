package com.gustate.uotan.user.data.api

import com.google.gson.Gson
import com.gustate.uotan.BuildConfig
import com.gustate.uotan.user.data.model.User
import com.gustate.uotan.utils.Utils.Companion.baseUrl
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.convertCookiesToString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MineApiService {
    suspend fun getMeUserInfo(
        onSuccess: (User) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = OkHttpClient.Builder()
                .build()
            val request = Request.Builder()
                .url("$baseUrl/api/me/")
                .addHeader("User-Agent", "UotanApp/1.0")
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("Cookie", convertCookiesToString(Cookies))
                .build()
            val json = client.newCall(request).execute().body?.string()
            val user = Gson().fromJson(json, User::class.java)
            withContext(Dispatchers.Main) {
                onSuccess(user)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onException(e)
            }
        }
    }
}