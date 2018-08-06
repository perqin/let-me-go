package com.perqin.letmego.data.preferences

import android.preference.PreferenceManager
import com.perqin.letmego.App
import com.perqin.letmego.data.place.Place

/**
 * @author perqin
 */
object PreferencesRepo {
    private const val PK_MY_LOCATION_CACHE_LAT = "my_location_cache_lat"
    private const val PK_MY_LOCATION_CACHE_LNG = "my_location_cache_lng"
    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(App.context)

    fun saveMyLocation(place: Place?) {
        if (place != null) {
            sharedPreferences
                    .edit()
                    .putFloat(PK_MY_LOCATION_CACHE_LAT, place.latitude.toFloat())
                    .putFloat(PK_MY_LOCATION_CACHE_LNG, place.longitude.toFloat())
                    .apply()
        }
    }

    fun loadMyLocation(): Place? {
        val lat = sharedPreferences.getFloat(PK_MY_LOCATION_CACHE_LAT, Float.MIN_VALUE)
        val lng = sharedPreferences.getFloat(PK_MY_LOCATION_CACHE_LNG, Float.MIN_VALUE)
        return if (lat != Float.MIN_VALUE && lng != Float.MIN_VALUE) {
            Place(lat.toDouble(), lng.toDouble())
        } else {
            null
        }
    }
}