package com.perqin.letmego.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier

class MainActivityViewModel : ViewModel() {
    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace

    private val _enableNotificationForSelectedPlace = MutableLiveData<Boolean>()
    val enableNotificationForSelectedPlace: LiveData<Boolean> = _enableNotificationForSelectedPlace

    init {
        _enableNotificationForSelectedPlace.value = false
    }

    fun deselectPlace() {
        _selectedPlace.value = null
    }

    fun selectPlace(latitude: Double, longitude: Double) {
        _selectedPlace.value = Place(latitude, longitude)
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

    fun activityCreate() {
        PlaceNotifier.startup()
    }

    fun activityDestroy() {
        // TODO: Only shutdown when no ongoing destination
        PlaceNotifier.shutdown()
    }
}
