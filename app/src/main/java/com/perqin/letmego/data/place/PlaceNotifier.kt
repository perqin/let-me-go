package com.perqin.letmego.data.place

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.perqin.letmego.App
import com.perqin.letmego.CHANNEL_ALERT
import com.perqin.letmego.R
import com.perqin.letmego.data.location.TencentLocator
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationUtils

/**
 * @author perqin
 */
object PlaceNotifier {
    private const val REQUEST_CODE_IGNORE_NOTIFICATION = 233
    private const val ACTION_IGNORE_NOTIFICATION = "com.perqin.letmego.ACTION_IGNORE_NOTIFICATION"

    private val myLocation = TencentLocator.getLocation()

    private val myLocationObserver = Observer<TencentLocation> {
        checkNotification(it.latitude, it.longitude)
    }

    private val ignoreNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == ACTION_IGNORE_NOTIFICATION) {
                stopVibration()
                notificationStatus = 2
            }
        }
    }

    private var destination: Place? = null
    private var activeDestinationLiveDataList = emptyList<MutableLiveData<Place?>>()

    fun getDestinationLiveData(): LiveData<Place?> {
        return object : MutableLiveData<Place?>() {
            override fun onActive() {
                activeDestinationLiveDataList += this
                value = destination
            }

            override fun onInactive() {
                activeDestinationLiveDataList -= this
            }
        }
    }

    fun setDestination(place: Place?) {
        destination = place
        activeDestinationLiveDataList.forEach {
            it.value = destination
        }
    }

    /**
     * 0: Idle
     * 1: Notifying
     * 2: Ignored (user've noticed the notification)
     */
    private var notificationStatus = 0

    fun enableNotificationForPlace(place: Place) {
        destination = place
        ensureLocation()
        App.context.registerReceiver(ignoreNotificationReceiver, IntentFilter(ACTION_IGNORE_NOTIFICATION))
    }

    fun disableNotification() {
        destination = null
        App.context.unregisterReceiver(ignoreNotificationReceiver)
    }

    fun startup() {
        ensureLocation()
    }

    fun shutdown() {
        myLocation.removeObserver(myLocationObserver)
        TencentLocator.disable()
    }

    private fun ensureLocation() {
        TencentLocator.enable()
        myLocation.observeForever(myLocationObserver)
    }

    private fun checkNotification(latitude: Double, longitude: Double) {
        if (destination != null) {
            val distance = TencentLocationUtils.distanceBetween(latitude, longitude, destination!!.latitude, destination!!.longitude)
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
