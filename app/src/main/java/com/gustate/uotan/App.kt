package com.gustate.uotan

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.gustate.uotan.settings.data.SettingModel.Companion.SSL_AUTH_DISABLE_KEY
import com.gustate.uotan.settings.data.SettingsRepository
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.room.UserDatabase
import com.haoge.easyandroid.EasyAndroid
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager


class App : Application() {
    private lateinit var fetch: Fetch

    companion object {
        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                val color = getThemeColor(context, R.attr.colorOnCardSecondary)
                ClassicsHeader(context).apply {
                    setArrowResource(R.drawable.arrow_down)
                    setProgressResource(R.drawable.ic_loading)
                    setDrawableArrowSize(24f)
                    setDrawableProgressSize(18f)
                    setAccentColor(color)
                    setSpinnerStyle(SpinnerStyle.Scale)
                }
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                val color = getThemeColor(context, R.attr.colorOnCardSecondary)
                ClassicsFooter(context).apply {
                    setArrowResource(R.drawable.arrow_down)
                    setProgressResource(R.drawable.ic_loading)
                    setDrawableSize(18f)
                    setAccentColor(color)
                    setSpinnerStyle(SpinnerStyle.Scale)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        EasyAndroid.init(this)
        lazy { UserDatabase.getDatabase(this) }
        val fetchConfiguration = FetchConfiguration.Builder(this)
            .enableLogging(true)
            .setDownloadConcurrentLimit(4)
            .setNamespace("UotanDownloads")
            .setAutoRetryMaxAttempts(3)
            .enableAutoStart(true)
            .build()
        fetch = Fetch.getInstance(fetchConfiguration)
        DialogX.globalStyle = MaterialYouStyle()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
    fun getFetch() = fetch
}