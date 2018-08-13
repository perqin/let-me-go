package com.perqin.letmego

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.perqin.letmego.notification.CHANNEL_ALERT
import com.perqin.letmego.notification.CHANNEL_TRACKING_FOREGROUND_SERVICE
import com.tencent.bugly.crashreport.CrashReport

/**
 * Created on 7/21/18.
 *
 * @author perqin
 */

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        context = this

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }

        // Tencent Bugly
        CrashReport.initCrashReport(this)
        CrashReport.setIsDevelopmentDevice(this, BuildConfig.DEBUG)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).run {
            createNotificationChannel(NotificationChannel(CHANNEL_ALERT, "Alert", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Alert that user is about to arrive"
            })
            createNotificationChannel(NotificationChannel(CHANNEL_TRACKING_FOREGROUND_SERVICE, "Tracking foreground service", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Tracking foreground service"
            })
        }
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}
