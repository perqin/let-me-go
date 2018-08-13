package com.perqin.letmego.services

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import com.perqin.letmego.App
import com.perqin.letmego.data.place.PlaceNotifier
import com.perqin.letmego.notification.NOTIFICATION_ID_TRACKING_FOREGROUND_SERVICE
import com.perqin.letmego.notification.createTrackingForegroundServiceNotification

class TrackingService : Service() {
    private var myLocationClientCount = 0
    private val binder = LocalBinder()

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_START_TRACKING) {
            if (!PlaceNotifier.isArrived()) {
                startForeground(NOTIFICATION_ID_TRACKING_FOREGROUND_SERVICE, createTrackingForegroundServiceNotification())
            } else {
                stopSelf()
            }
        } else if (intent.action == ACTION_STOP_TRACKING) {
            stopForeground(true)
            stopSelf()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private fun increaseMyLocationClient() {
        ++myLocationClientCount
        PlaceNotifier.myLocationRequired = true
    }

    private fun decreaseMyLocationClient() {
        --myLocationClientCount
        if (myLocationClientCount == 0) {
            PlaceNotifier.myLocationRequired = false
        }
    }

    companion object {
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
