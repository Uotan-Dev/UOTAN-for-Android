package com.gustate.uotan.user.data.api

import android.util.Log
import com.google.gson.Gson
import com.gustate.uotan.BuildConfig
import com.gustate.uotan.user.data.model.MeModel
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.network.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request

class MineApiService {
    suspend fun getMeUserInfo(
        onSuccess: (MeModel) -> Unit,
        onException: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val client = HttpClient.getClient()
            val request = Request.Builder()
                .url("$baseUrl/api/me/")
                .addHeader("User-Agent", "UotanApp/1.0")
                .addHeader("XF-Api-Key", BuildConfig.xfApiKey)
                .addHeader("XF-Api-User",
                    HttpClient.getAllCookies()["xf_user"] ?: "")
                .build()
            val json = client.newCall(request).execute().body.string()
            val user = Gson().fromJson(json, MeModel::class.java)

            Log.e("e", json)
            withContext(Dispatchers.Main) {
                onSuccess(user)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("e", e.message ?:"")
                onException(e)
            }
        }
    }
}