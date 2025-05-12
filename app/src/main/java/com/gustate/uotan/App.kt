package com.gustate.uotan

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.gustate.uotan.utils.Utils.Companion.getThemeColor
import com.gustate.uotan.utils.room.AppDatabase
import com.haoge.easyandroid.EasyAndroid
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration


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
                }
            }
            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                val color = getThemeColor(context, R.attr.colorOnCardSecondary)
                ClassicsFooter(context).apply {
                    setArrowResource(R.drawable.arrow_down)
                    setProgressResource(R.drawable.ic_loading)
                    setDrawableSize(18f)
                    setAccentColor(color)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        DialogX.init(this)
        EasyAndroid.init(this)
        lazy { AppDatabase.getDatabase(this) }
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