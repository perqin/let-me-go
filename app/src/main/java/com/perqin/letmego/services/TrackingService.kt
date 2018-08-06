package com.perqin.letmego.services

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Binder
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.perqin.letmego.App
import com.perqin.letmego.CHANNEL_TRACKING_FOREGROUND_SERVICE
import com.perqin.letmego.R
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier

class TrackingService : Service() {
    private var myLocationClientCount = 0
    private val binder = LocalBinder()
    private val stopTrackingReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_STOP_TRACKING) {
                PlaceNotifier.disableNotification()
                stopForeground(true)
                unregisterReceiver(this)
            }
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_START_TRACKING) {
            startForeground(NOTIFICATION_TRACKING_FOREGROUND_SERVICE, createTrackingForegroundServiceNotification())
            PlaceNotifier.enableNotificationForPlace(Place(
                    intent.getDoubleExtra(EXTRA_DESTINATION_LAT, 0.0),
                    intent.getDoubleExtra(EXTRA_DESTINATION_LNG, 0.0)
            ))
            registerReceiver(stopTrackingReceiver, IntentFilter(ACTION_STOP_TRACKING))
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun increaseMyLocationClient() {
        ++myLocationClientCount
        PlaceNotifier.startup()
    }

    private fun decreaseMyLocationClient() {
        --myLocationClientCount
        if (myLocationClientCount == 0) {
            PlaceNotifier.shutdown()
        }
    }

    private fun createTrackingForegroundServiceNotification(): Notification {
        val context = App.context
        return NotificationCompat.Builder(context, CHANNEL_TRACKING_FOREGROUND_SERVICE)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_title_tracking_foreground_service))
                .setContentText(context.getString(R.string.notification_text_tracking_foreground_service))
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build()
    }

    companion object {
        const val EXTRA_DESTINATION_LAT = "destination_lat"
        const val EXTRA_DESTINATION_LNG = "destination_lng"
        const val NOTIFICATION_TRACKING_FOREGROUND_SERVICE = 1
        val ACTION_START_TRACKING = "${App.context.packageName}.ACTION_START_TRACKING"
        val ACTION_STOP_TRACKING = "${App.context.packageName}.ACTION_STOP_TRACKING"
    }

    inner class LocalBinder : Binder() {
        fun uiRequireMyLocationUpdates() {
            increaseMyLocationClient()
        }

        fun uiNotRequireMyLocationUpdates() {
            decreaseMyLocationClient()
        }
    }
}
