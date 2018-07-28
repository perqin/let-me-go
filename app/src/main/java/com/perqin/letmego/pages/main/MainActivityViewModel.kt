package com.perqin.letmego.pages.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier

class MainActivityViewModel : ViewModel() {
    val myLocation: LiveData<Place> = Transformations.map(TencentLocator.getLocation()) {
        Place(it.latitude, it.longitude)
    }

    private val _destination = MutableLiveData<Place?>()
    val destination: LiveData<Place?> = _destination

    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = _selectedPlace

    private val _enableNotificationForSelectedPlace = MutableLiveData<Boolean>()
    val enableNotificationForSelectedPlace: LiveData<Boolean> = _enableNotificationForSelectedPlace

    private val _mapCameraMode = MutableLiveData<MapCameraMode>()
    val mapCameraMode: LiveData<MapCameraMode> = _mapCameraMode

    private val _mapCameraTargets = MutableLiveData<List<Place>>()
    val mapCameraTargets: LiveData<List<Place>> = _mapCameraTargets

    init {
        _enableNotificationForSelectedPlace.value = false
        _mapCameraMode.value = MapCameraMode.CENTER_MY_LOCATION
        _mapCameraTargets.value = emptyList()
        myLocation.observeForever {
            updateMapCamera()
        }
    }

    fun deselectPlace() {
        _selectedPlace.value = null
        updateMapCamera()
    }

    fun selectPlace(latitude: Double, longitude: Double) {
        _selectedPlace.value = Place(latitude, longitude)
        updateMapCamera()
    }

    fun toggleEnableNotificationForSelectedPlace() {
        if (_selectedPlace.value != null) {
            val enable = !_enableNotificationForSelectedPlace.value!!
            _enableNotificationForSelectedPlace.value = enable
            if (enable) {
                _destination.value = _selectedPlace.value
                enableNotification()
            } else {
                disableNotification()
            }
        }
        updateMapCamera()
    }

    fun activityCreate() {
        PlaceNotifier.startup()
    }

    fun activityDestroy() {
        // TODO: Only shutdown when no ongoing destination
        PlaceNotifier.shutdown()
    }

    fun rotateMapCameraMode() {
        _mapCameraMode.value = when (_mapCameraMode.value?: MapCameraMode.FREE) {
            MapCameraMode.FREE -> MapCameraMode.CENTER_MY_LOCATION
            MapCameraMode.CENTER_MY_LOCATION ->
                if (_selectedPlace.value != null)
                    MapCameraMode.CENTER_TERMINALS
                else
                    MapCameraMode.FREE
            MapCameraMode.CENTER_TERMINALS -> MapCameraMode.FREE
        }
        updateMapCamera()
    }

    private fun enableNotification() {
        // TODO: Handle old destination
        PlaceNotifier.enableNotificationForPlace(_destination.value!!)
    }

    private fun disableNotification() {
        PlaceNotifier.disableNotification()
    }

    private fun updateMapCamera() {
        val mode = _mapCameraMode.value?: MapCameraMode.FREE
        val myLocation = this.myLocation.value
        val selectedPlace = this.selectedPlace.value
        _mapCameraTargets.value = if (myLocation != null) {
            when (mode) {
                MainActivityViewModel.MapCameraMode.FREE -> emptyList()
                MainActivityViewModel.MapCameraMode.CENTER_MY_LOCATION -> listOf(myLocation)
                MainActivityViewModel.MapCameraMode.CENTER_TERMINALS -> listOf(myLocation, selectedPlace!!)
            }
        } else {
            emptyList()
        }
    }

    enum class MapCameraMode { FREE, CENTER_MY_LOCATION, CENTER_TERMINALS }
}
