package com.perqin.letmego.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.perqin.letmego.App
import com.perqin.letmego.R
import com.perqin.letmego.pages.main.MainActivity
import com.perqin.letmego.receiver.NotificationReceiver

/**
 * @author perqin
 */
const val NOTIFICATION_ID_TRACKING_FOREGROUND_SERVICE = 1
const val REQUEST_START_MAIN_ACTIVITY = 100
const val REQUEST_STOP_TRACKING = 101
const val CHANNEL_ALERT = "com.perqin.letmego.CHANNEL_ALERT"
const val CHANNEL_TRACKING_FOREGROUND_SERVICE = "CHANNEL_TRACKING_FOREGROUND_SERVICE"

fun createTrackingForegroundServiceNotification(): Notification {
    val context = App.context
    return NotificationCompat.Builder(context, CHANNEL_TRACKING_FOREGROUND_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title_tracking_foreground_service))
            .setContentText(context.getString(R.string.notification_text_tracking_foreground_service))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(PendingIntent.getActivity(context, REQUEST_START_MAIN_ACTIVITY,
                    Intent(context, MainActivity::class.java), 0))
            .build()
}

fun createArrivalNotification(): Notification {
    val context = App.context
    return NotificationCompat.Builder(context, CHANNEL_TRACKING_FOREGROUND_SERVICE)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_title_arrival))
            .setContentText(context.getString(R.string.notification_text_arrival))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(PendingIntent.getBroadcast(context, REQUEST_STOP_TRACKING,
                    Intent(context, NotificationReceiver::class.java).apply { action = NotificationReceiver.ACTION_STOP_TRACKING }, 0))
            .build()
}
