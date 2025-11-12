package com.gustate.uotan.utils.network

import android.content.Context
import com.gustate.uotan.utils.Utils.TIMEOUT_MS
import com.gustate.uotan.utils.Utils.baseUrl
import com.gustate.uotan.utils.network.cookiejar.PersistentCookieJar
import com.gustate.uotan.utils.network.cookiejar.cache.SetCookieCache
import com.gustate.uotan.utils.network.cookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.collections.set

object HttpClient {
    private lateinit var cookieJar: PersistentCookieJar
    private lateinit var client: OkHttpClient

    fun initialize(context: Context) {
        val cookieCache = SetCookieCache()
        val cookiePersistor = SharedPrefsCookiePersistor(context)
        cookieJar = PersistentCookieJar(cookieCache, cookiePersistor)

        client = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(TIMEOUT_MS.toLong(), TimeUnit.MILLISECONDS)
            .build()
    }

    fun getClient(): OkHttpClient {
        if (!::client.isInitialized) {
            throw IllegalStateException("HttpClient not initialized. Call initialize() first.")
        }
        return client
    }

    fun getCookieJar(): PersistentCookieJar {
        return cookieJar
    }

    fun clearCookies() {
        cookieJar.clear()
    }

    /**
     * 获取全部 Cookie
     * @return Map<name: String, value: String>
     */
    fun getAllCookies(): Map<String, String> {
        val cookieMap = mutableMapOf<String, String>()
        val cookies = cookieJar.loadForRequest(baseUrl.toHttpUrl())
        cookies.forEach { cookie ->
            cookieMap[cookie.name] = cookie.value
        }
        return cookieMap
    }
}