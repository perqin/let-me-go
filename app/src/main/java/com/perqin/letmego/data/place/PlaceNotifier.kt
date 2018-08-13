package com.perqin.letmego.data.place

import android.content.Context
import android.content.Intent
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.perqin.letmego.App
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.notification.NOTIFICATION_ID_TRACKING_FOREGROUND_SERVICE
import com.perqin.letmego.notification.createArrivalNotification
import com.perqin.letmego.services.TrackingService
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationUtils

/**
 * @author perqin
 */
object PlaceNotifier {
    var myLocationRequired = false
        set(value) {
            field = value
            if (value) {
                TencentLocator.enable()
            } else if (destination == null) {
                TencentLocator.disable()
            }
        }

    private val myLocation = TencentLocator.getLocation()

    private val myLocationObserver = Observer<TencentLocation> {
        checkNotification(it.latitude, it.longitude)
    }

    private var destination: Place? = null
        set(value) {
            field = value
            activeDestinationLiveDataList.forEach { it.value = value }
        }
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

    /**
     * 0: Idle
     * 1: Notifying
     */
    private var notificationStatus = 0
    fun isArrived() = notificationStatus == 1

    /**
     * @param destination if it is the same as this.destination or null, stop tracking, otherwise update tracking destination
     */
    fun setOrUnsetDestination(destination: Place?) {
        if (destination == null || (this.destination != null && Place.isEqual(this.destination!!, destination))) {
            this.destination = null
            // Stop tracking
            myLocation.removeObserver(myLocationObserver)
            App.context.run { startService(Intent(this, TrackingService::class.java).apply {
                action = TrackingService.ACTION_STOP_TRACKING
            }) }
            // Stop notification is needed
            if (notificationStatus == 1) {
                notificationStatus = 0
                stopVibration()
            }
            // Stop locating if unnecessary
            if (!myLocationRequired) {
                TencentLocator.disable()
            }
        } else {
            this.destination = destination
            // Start tracking new destination
            myLocation.observeForever(myLocationObserver)
            App.context.run { startService(Intent(this, TrackingService::class.java).apply {
                action = TrackingService.ACTION_START_TRACKING
            }) }
            // Start locating
            TencentLocator.enable()
        }
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
            NotificationManagerCompat.from(App.context).notify(NOTIFICATION_ID_TRACKING_FOREGROUND_SERVICE,
                    createArrivalNotification())
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
