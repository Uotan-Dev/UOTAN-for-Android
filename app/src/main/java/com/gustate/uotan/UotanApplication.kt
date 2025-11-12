package com.gustate.uotan

import android.app.Application
import android.webkit.WebView
import com.google.android.material.color.DynamicColors
import com.gustate.uotan.utils.Utils.getThemeColor
import com.gustate.uotan.utils.network.HttpClient
import com.gustate.uotan.utils.room.UserDatabase
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.scwang.smart.refresh.layout.constant.SpinnerStyle
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import dagger.hilt.android.HiltAndroidApp

/**
 * UotanApplication
 * Time 09/22/2025
 * Fix initial Database
 * @see Application
 * @see Fetch
 * @see SmartRefreshLayout
 * @see UserDatabase
 */
@HiltAndroidApp
class UotanApplication : Application() {

    // Fetch 下载器 (延迟初始化)
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
                    setSpinnerStyle(SpinnerStyle.FixedBehind)
                }
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                val color = getThemeColor(context, R.attr.colorOnCardSecondary)
                ClassicsFooter(context).apply {
                    setArrowResource(R.drawable.arrow_down)
                    setProgressResource(R.drawable.ic_loading)
                    setDrawableSize(18f)
                    setAccentColor(color)
                    setSpinnerStyle(SpinnerStyle.FixedBehind)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(true)
        HttpClient.initialize(this)
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

    /**
     * 获取 Fetch 实例
     * @return Fetch
     */
    fun getFetch() = fetch
}