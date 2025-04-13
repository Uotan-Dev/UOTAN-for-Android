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
    override fun onCreate() {
        super.onCreate()
        DialogX.init(this)
        EasyAndroid.init(this)
        lazy { AppDatabase.getDatabase(this) }
        Fetch.Impl.getInstance(
            FetchConfiguration.Builder(this)
                .setDownloadConcurrentLimit(4)
                .setNamespace("UotanDownloads")
                .setAutoRetryMaxAttempts(3)
                .enableAutoStart(true)
                .build()
        )
        DialogX.globalStyle = MaterialYouStyle()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}