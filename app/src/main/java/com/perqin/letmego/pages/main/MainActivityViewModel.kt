package com.perqin.letmego.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.model.LatLng
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier

class MainActivityViewModel : ViewModel() {
    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace

    private val _enableNotificationForSelectedPlace = MutableLiveData<Boolean>()
    val enableNotificationForSelectedPlace: LiveData<Boolean> = _enableNotificationForSelectedPlace

    // So dirty...
    private val _locationSource = MutableLiveData<LocationSource>()
    val locationSource: LiveData<LocationSource> = _locationSource

    init {
        _locationSource.value = PlaceNotifier.locationSource
        _enableNotificationForSelectedPlace.value = false
    }

    fun deselectPlace() {
        _selectedPlace.value = null
    }

    fun selectPlace(latLng: LatLng) {
        _selectedPlace.value = Place(latLng)
    }

    fun toggleEnableNotificationForSelectedPlace() {
        if (_selectedPlace.value != null) {
            val enable = !_enableNotificationForSelectedPlace.value!!
            _enableNotificationForSelectedPlace.value = enable
            if (enable) {
                PlaceNotifier.enableNotificationForPlace(_selectedPlace.value!!)
            } else {
                PlaceNotifier.disableNotification()
            }
        }
    }

    fun shutdownService() {
        PlaceNotifier.shutdown()
    }
}
