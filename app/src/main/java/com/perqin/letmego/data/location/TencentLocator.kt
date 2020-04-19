package com.perqin.letmego.data.location

import androidx.lifecycle.LiveData
import com.perqin.letmego.App
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationRequest

/**
 * Created by perqin on 2018/07/28.
 */
object TencentLocator {
    var lastLocation: TencentLocation? = null
        private set
    private var listeners = emptyList<OnLocationUpdateListener>()
    private val tencentLocationListener = object : TencentLocationListener {
        override fun onStatusUpdate(p0: String?, p1: Int, p2: String?) {
            // TODO: Notify user to enable GPS if needed
        }

        override fun onLocationChanged(location: TencentLocation, error: Int, reason: String?) {
            if (TencentLocation.ERROR_OK == error) {
                lastLocation = location
                listeners.forEach {
                    it.onLocationUpdate(location)
                }
            }
        }
    }

    fun enable() {
        val request = TencentLocationRequest.create().apply {
            interval = 5000L
            requestLevel = TencentLocationRequest.REQUEST_LEVEL_GEO
            isAllowCache = true
        }
        val error = TencentLocationManager.getInstance(App.context).requestLocationUpdates(request, tencentLocationListener)
        println("PlaceNotifier.ensureLocation: TencentLocationManager.requestLocationUpdates: error = $error")
    }

    fun disable() {
        TencentLocationManager.getInstance(App.context).removeUpdates(tencentLocationListener)
    }

    fun getLocation(): LiveData<TencentLocation> {
        return object : LiveData<TencentLocation>(), OnLocationUpdateListener {
            override fun onActive() {
                listeners += this
            }

            override fun onInactive() {
                listeners -= this
            }

            override fun onLocationUpdate(tencentLocation: TencentLocation) {
                value = tencentLocation
            }
        }
    }

    interface OnLocationUpdateListener {
        fun onLocationUpdate(tencentLocation: TencentLocation)
    }
}
