package com.gustate.uotan

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.gustate.uotan.utils.room.AppDatabase
import com.haoge.easyandroid.EasyAndroid
import com.kongzue.dialogx.DialogX
import com.kongzue.dialogxmaterialyou.style.MaterialYouStyle
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration

class App : Application() {
    private lateinit var fetch: Fetch
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