package com.perqin.letmego.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.amap.api.maps2d.model.LatLng

class MainActivityViewModel : ViewModel() {
    private val _selectedPlace = MutableLiveData<PlaceOnMap?>()
    val selectedPlace: LiveData<PlaceOnMap?> = _selectedPlace

    fun deselectPlace() {
        _selectedPlace.value = null
    }

    fun selectPlace(latLng: LatLng) {
        _selectedPlace.value = PlaceOnMap(latLng)
    }

    data class PlaceOnMap(
            var latLng: LatLng
    )
}
