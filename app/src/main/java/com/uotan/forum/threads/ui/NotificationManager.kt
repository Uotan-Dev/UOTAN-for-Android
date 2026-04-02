package com.uotan.forum.threads.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.content.Context
import androidx.core.app.NotificationCompat
import com.uotan.forum.R

object NotificationManager {
    private lateinit var notificationManager: NotificationManager
    private lateinit var appContext: Context
    const val CHANNEL_ID = "tun"
    private const val NOTIFICATION_ID = 1001

    fun initialize(context: Context, notifManager: NotificationManager) {
        notificationManager = notifManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.thread_live_update_notification),
            IMPORTANCE_DEFAULT
        )
        appContext = context
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(title: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setRequestPromotedOngoing(true)
            .setContentTitle(title)
            .setContentText("续读阅读")
            .setShortCriticalText("Back!")
    }

    fun postNotification(title: String) {
        val notification = buildNotification(title).build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}