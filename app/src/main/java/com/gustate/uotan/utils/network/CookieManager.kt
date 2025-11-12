package com.gustate.uotan.utils.network

import android.annotation.SuppressLint
import android.util.Log
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrl

class CookieManager() {

    private val cookieManager = android.webkit.CookieManager.getInstance()
    private val cookieJar = HttpClient.getCookieJar()

    @SuppressLint("SetJavaScriptEnabled")
    fun setupCookieManager(webView: android.webkit.WebView) {
        cookieManager.setAcceptCookie(true)
        cookieManager.setAcceptThirdPartyCookies(webView, true)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
        }
    }

    /**
     * 将 WebView 的 Cookie 同步到 PersistentCookieJar
     */
    fun syncWebViewCookiesToJar(url: String) {
        try {
            val cookieString = cookieManager.getCookie(url)
            Log.e("CookieManager", "获取到Cookie: $cookieString")
            cookieString?.let { cookies ->
                // 将Cookie字符串转换为OkHttp的Cookie对象
                val okHttpCookies = parseCookieString(url, cookies)
                val cookies = mutableListOf<Cookie>()
                okHttpCookies.forEach { cookie ->
                    cookies.add(cookie)
                }
                Log.e("saveCookie", cookies.toString())
                cookieJar.saveFromResponse(url.toHttpUrl(), cookies)
            }
        } catch (e: Exception) {
            Log.e("CookieManager", "同步Cookie失败: ${e.message}")
        }
    }

    /**
     * 将PersistentCookieJar的Cookie设置到WebView
     */
    fun syncJarCookiesToWebView(url: String) {
        try {
            val httpUrl = url.toHttpUrl()
            val cookies = cookieJar.loadForRequest(httpUrl)
            cookies.forEach { cookie ->
                val cookieString = "${cookie.name}=${cookie.value}; domain=${cookie.domain}; path=${cookie.path}"
                cookieManager.setCookie(url, cookieString)
            }
            cookieManager.flush()
        } catch (e: Exception) {
            Log.e("CookieManager", "设置Cookie到WebView失败: ${e.message}")
        }
    }

    private fun parseCookieString(url: String, cookieString: String): List<Cookie> {
        val cookies = mutableListOf<Cookie>()
        cookieString.split(";").forEach { cookiePair ->
            val parts = cookiePair.trim().split("=")
            try {
                val cookie = Cookie.Builder()
                    .name(parts[0])
                    .value(parts[1])
                    .domain(url.toHttpUrl().host)
                    .path("/")
                    .build()
                cookies.add(cookie)
            } catch (e: Exception) {
                Log.e("CookieManager", "解析Cookie失败: $cookiePair ${e.message}")
            }

        }
        return cookies
    }
}