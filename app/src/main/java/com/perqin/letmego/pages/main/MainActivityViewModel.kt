package com.perqin.letmego.pages.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.perqin.letmego.App
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

    private val _grantedPermissions = MutableLiveData<Set<String>>()
    val grantedPermissions: LiveData<Set<String>> = _grantedPermissions

    val allPermissionsGranted: LiveData<Boolean> = Transformations.map(_grantedPermissions) {
        (_grantedPermissions.value?: emptySet()).containsAll(permissionsList)
    }

    private var mapPrepared = false

    init {
        _enableNotificationForSelectedPlace.value = false
        _mapCameraMode.value = MapCameraMode.CENTER_MY_LOCATION
        _mapCameraTargets.value = emptyList()
        _grantedPermissions.value = emptySet()
        myLocation.observeForever { updateMapCameraTargets() }
        _destination.observeForever { updateMapCameraTargets() }
        _selectedPlace.observeForever { updateMapCameraTargets() }
        _mapCameraMode.observeForever { updateMapCameraTargets() }
        _grantedPermissions.observeForever {
            if (!mapPrepared && it.containsAll(permissionsList)) {
                prepareMap()
                mapPrepared = true
            }
        }
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
                _destination.value = _selectedPlace.value
                enableNotification()
            } else {
                disableNotification()
            }
        }
    }

    fun activityCreate() {
        permissionsList.forEach {
            if (ContextCompat.checkSelfPermission(App.context, it) == PackageManager.PERMISSION_GRANTED)
                _grantedPermissions.value = (_grantedPermissions.value?: emptySet()) + it
        }
    }

    private fun prepareMap() {
        PlaceNotifier.startup()
    }

    fun activityDestroy() {
        if (mapPrepared) {
            // TODO: Only shutdown when no ongoing destination
            PlaceNotifier.shutdown()
        }
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
    }

    private fun enableNotification() {
        // TODO: Handle old destination
        PlaceNotifier.enableNotificationForPlace(_destination.value!!)
    }

    private fun disableNotification() {
        PlaceNotifier.disableNotification()
    }

    private fun updateMapCameraTargets() {
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

    fun freeMapCamera() {
        if (_mapCameraMode.value != MapCameraMode.FREE) {
            _mapCameraMode.value = MapCameraMode.FREE
        }
    }

    fun permissionsGranted(vararg permissions: String) {
        _grantedPermissions.value = (_grantedPermissions.value?: emptySet()) + permissions
    }

    companion object {
        val permissionsList = listOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    enum class MapCameraMode { FREE, CENTER_MY_LOCATION, CENTER_TERMINALS }
}
