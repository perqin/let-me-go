package com.perqin.letmego.data.place

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps2d.AMapUtils
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.model.LatLng
import com.perqin.letmego.App
import com.perqin.letmego.CHANNEL_ALERT
import com.perqin.letmego.R

/**
 * @author perqin
 */
object PlaceNotifier {
    private const val REQUEST_CODE_IGNORE_NOTIFICATION = 233
    private const val ACTION_IGNORE_NOTIFICATION = "com.perqin.letmego.ACTION_IGNORE_NOTIFICATION"

    private val ignoreNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent) {
            if (p1.action == ACTION_IGNORE_NOTIFICATION) {
                stopVibration()
                notificationStatus = 2
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private var locationClient: AMapLocationClient? = null

    private var onLocationChangedListener: LocationSource.OnLocationChangedListener? = null

    private var destination: Place? = null

    /**
     * 0: Idle
     * 1: Notifying
     * 2: Ignored (user've noticed the notification)
     */
    private var notificationStatus = 0

    val locationSource = object : LocationSource {
        override fun activate(listener: LocationSource.OnLocationChangedListener) {
            onLocationChangedListener = listener
        }

        override fun deactivate() {
            onLocationChangedListener = null
        }
    }
        get() {
            ensureLocation()
            return field
        }

    fun enableNotificationForPlace(place: Place) {
        destination = place
        ensureLocation()
        App.context.registerReceiver(ignoreNotificationReceiver, IntentFilter(ACTION_IGNORE_NOTIFICATION))
    }

    fun disableNotification() {
        destination = null
        App.context.unregisterReceiver(ignoreNotificationReceiver)
    }

    fun shutdown() {
        locationClient?.run {
            stopLocation()
            onDestroy()
        }
        locationClient = null
    }

    private fun ensureLocation() {
        if (locationClient == null) {
            locationClient = AMapLocationClient(App.context).apply {
                setLocationListener {
                    println("PERQIN: ${it.latitude}, ${it.longitude} ${it.errorCode}, ${it.errorInfo}")
                    onLocationChangedListener?.onLocationChanged(it)
                    checkNotification(it)
                }
                startLocation()
            }
        }
    }

    private fun checkNotification(location: Location) {
        if (destination != null) {
            val distance = AMapUtils.calculateLineDistance(destination!!.latLng, LatLng(location.latitude, location.longitude))
            if (distance <= 500) {
                sendAboutToArriveNotification()
            }
        }
    }

    private fun sendAboutToArriveNotification() {
        if (notificationStatus == 0) {
            notificationStatus = 1
            // Send system notification
            val notification = NotificationCompat.Builder(App.context, CHANNEL_ALERT)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(App.context.getString(R.string.app_name))
                    .setContentText("You are about to arrive!!!")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getBroadcast(
                            App.context,
                            REQUEST_CODE_IGNORE_NOTIFICATION,
                            Intent(ACTION_IGNORE_NOTIFICATION),
                            0
                    ))
                    .build()
            NotificationManagerCompat.from(App.context).notify(233, notification)
            // Start vibration
            startVibration()
        }
    }

    private fun startVibration() {
        (App.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(longArrayOf(0L, 2000L, 2000L), 0)
    }

    private fun stopVibration() {
        (App.context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).cancel()
    }
}
