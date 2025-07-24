package com.gustate.uotan

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY
import com.gustate.uotan.settings.data.SettingsRepository
import com.gustate.uotan.utils.Utils.Companion.Cookies
import com.gustate.uotan.utils.Utils.Companion.isLogin
import com.gustate.uotan.utils.Utils.Companion.openImmersion
import com.gustate.uotan.utils.parse.data.CookiesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

open class BaseActivity : AppCompatActivity() {

    var currentThemeResId: Int = 0

    private val cookiesManager by lazy { CookiesManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        val settingsRepository = SettingsRepository(this)

        lifecycleScope.launch {
            setCookies()
            isLogin = isLogin()
            if (settingsRepository.getSettingByKey(SSL_AUTH_DISABLE_KEY).value.toBoolean()) {
                val sslContext = SSLContext.getInstance("TLSv1.2")
                sslContext.init(null, arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate?>?,
                        authType: String?
                    ) { }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate?>?,
                        authType: String?
                    ) { }

                    override fun getAcceptedIssuers(): Array<out X509Certificate?>? {
                        return emptyArray()
                    }

                }), SecureRandom())
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
                // 4. 设置全局主机名验证器 (信任所有主机名)
                HttpsURLConnection.setDefaultHostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true })
            }
        }
        enableEdgeToEdge()
        openImmersion(window)
        super.onCreate(savedInstanceState)
    }

    private fun applyTheme() {
        val themeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        currentThemeResId = themeResId
        setTheme(themeResId)
    }

    override fun onResume() {
        checkAndRecreateIfThemeChanged()
        lifecycleScope.launch {
            setCookies()
            isLogin = isLogin()
        }
        enableEdgeToEdge()
        openImmersion(window)
        super.onResume()
    }

    private fun checkAndRecreateIfThemeChanged() {
        val newThemeResId = when (ThemePreference.getTheme(this)) {
            "base" -> R.style.Base_Theme_Uotan
            "monet" -> R.style.Monet_Theme_Uotan
            else -> R.style.Base_Theme_Uotan
        }
        if (newThemeResId != currentThemeResId) {
            Handler(Looper.getMainLooper()).post {
                currentThemeResId = newThemeResId
                recreate()
            }
        }
    }

    private suspend fun setCookies() = withContext(Dispatchers.IO) {
        // 将数据库中的 Cookies 赋值到全局变量中
        Cookies = cookiesManager.cookiesFlow.first()
    }

    private fun isLogin() = Cookies != mapOf<String, String>()

}