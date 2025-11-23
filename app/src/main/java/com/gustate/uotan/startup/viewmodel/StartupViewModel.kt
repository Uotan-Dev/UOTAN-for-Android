package com.gustate.uotan.startup.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.gustate.uotan.settings.data.SettingModel
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY
import com.gustate.uotan.settings.data.SettingsRepository
import com.gustate.uotan.startup.data.parse.StartupParse
import com.gustate.uotan.startup.ui.StartupState
import com.gustate.uotan.utils.Utils.baseUrl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class StartupViewModel(context: Application): AndroidViewModel(context) {

    private val sr = SettingsRepository(context)
    private val sp = StartupParse()
    private val _countdown = MutableStateFlow(5)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()
    private val _uiState = MutableStateFlow<StartupState>(StartupState.Idle)
    val uiState: StateFlow<StartupState> = _uiState.asStateFlow()

    init {
        countDown()
        startup()
    }
    private fun countDown() {
        viewModelScope.launch {
            // 启动倒计时协程
            val countdownJob = launch {
                while (_countdown.value > 0) {
                    delay(1000)
                    _countdown.value -= 1
                }
            }
        }
    }

    fun startup() {
        _uiState.value = StartupState.Loading
        viewModelScope.launch {
            initInternetConfig()
            val result = sp.parseStartupType()
            when {
                result.isSuccess -> {
                    val data = result.getOrNull()!!
                    if (data.isSmsVerify) _uiState.value = StartupState.NeedSmsVerify
                    else if (data.isAgreement)
                        _uiState.value = StartupState.NeedAgreePrivacy(data.agreementXfToken)
                    else _uiState.value = StartupState.Success
                }
                else -> {
                    val exception = result.exceptionOrNull()
                    _uiState.value = StartupState.Error(exception?.message ?: "未知错误")
                }
            }
        }
    }

    /**
     * 用于网络测试
     * TODO 记得删
     */
    private suspend fun initInternetConfig() = withContext(Dispatchers.IO) {
        val domain = sr.getSettingByKey(SettingModel.DOMAIN_CUSTOM_VALUE_KEY).value
        baseUrl = if (!domain.startsWith("http")) "https://$domain" else domain
        if (sr.getSettingByKey(SSL_AUTH_DISABLE_KEY).value.toBoolean()) {
            val sslContext = SSLContext.getInstance("TLSv1.2")
            sslContext.init(null,
                arrayOf<TrustManager>(@SuppressLint("CustomX509TrustManager")
                object : X509TrustManager {
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate?>?,
                        authType: String?
                    ) { }
                    @SuppressLint("TrustAllX509TrustManager")
                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate?>?,
                        authType: String?
                    ) { }
                    override fun getAcceptedIssuers(): Array<out X509Certificate?>? {
                        return emptyArray()
                    }
                }), SecureRandom())
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.socketFactory)
            // 设置信任所有主机名
            HttpsURLConnection.setDefaultHostnameVerifier(
                { hostname: String?, session: SSLSession? -> true }
            )
        }
    }
}