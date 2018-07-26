package com.perqin.letmego.data.place

import android.annotation.SuppressLint
import com.amap.api.location.AMapLocationClient

/**
 * @author perqin
 */
object PlaceNotifier {
    @SuppressLint("StaticFieldLeak")
    private var locationClient: AMapLocationClient? = null

    fun enableNotificationForPlace(place: Place) {}

    fun disableNotificationForPlace(place: Place) {}
}
