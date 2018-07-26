package com.perqin.letmego

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Created on 7/21/18.
 *
 * @author perqin
 */
val CHANNEL_ALERT = "com.perqin.letmego.CHANNEL_ALERT"

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(NotificationChannel(CHANNEL_ALERT, "Alert", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Alert that user is about to arrive"
                })
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}
