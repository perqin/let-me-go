package com.perqin.letmego.pages.main

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.*
import com.perqin.letmego.App
import com.perqin.letmego.data.api.TencentLbsApi
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier
import com.perqin.letmego.data.placeinfo.PlaceInfo
import com.perqin.letmego.data.preferences.PreferencesRepo
import com.perqin.letmego.services.TrackingService
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext

class MainActivityViewModel : ViewModel() {
    private val myLocationFromLocator: LiveData<Place> = Transformations.map(TencentLocator.getLocation()) {
        Place(it.latitude, it.longitude)
    }
    private val _myLocationCache = MutableLiveData<Place?>()
    val myLocation: LiveData<Place> = MediatorLiveData<Place>().apply {
        addSource(_myLocationCache) {
            if (value == null) {
                value = it
            }
        }
        addSource(myLocationFromLocator) {
            value = it
        }
    }

    val destination: LiveData<Place?> = PlaceNotifier.getDestinationLiveData()

    private val _selectedPlace = MutableLiveData<Place?>()
    val selectedPlace: LiveData<Place?> = object : MediatorLiveData<Place?>() {
        private var userSelectedPlace: Place? = null
        private var destination: Place? = null

        init {
            addSource(this@MainActivityViewModel.destination) {
                this.destination = it
                updateValue()
            }
            addSource(this@MainActivityViewModel._selectedPlace) {
                this.userSelectedPlace = it
                updateValue()
            }
        }

        private fun updateValue() {
            value = destination?: userSelectedPlace
        }
    }

    private val _selectedPlaceInfo = MutableLiveData<PlaceInfo?>()
    val selectedPlaceInfo: LiveData<PlaceInfo?> = _selectedPlaceInfo

    private val _enableNotificationForSelectedPlace = MutableLiveData<Boolean>()
    val enableNotificationForSelectedPlace: LiveData<Boolean> = _enableNotificationForSelectedPlace

    private val _mapCameraMode = MutableLiveData<MapCameraMode>()
    val mapCameraMode: LiveData<MapCameraMode> = _mapCameraMode

    private val _mapCameraTargets = MutableLiveData<List<Place>>()
    val mapCameraTargets: LiveData<List<Place>> = _mapCameraTargets

    private val _grantedPermissions = MutableLiveData<Set<String>>()
    val grantedPermissions: LiveData<Set<String>> = _grantedPermissions

    val allPermissionsGranted: LiveData<Boolean> = Transformations.map(_grantedPermissions) {
        it.containsAll(permissionsList)
    }

    init {
        _enableNotificationForSelectedPlace.value = false
        _mapCameraMode.value = MapCameraMode.CENTER_MY_LOCATION
        _mapCameraTargets.value = emptyList()
        _grantedPermissions.value = emptySet()
        myLocation.observeForever { updateMapCameraTargets() }
        destination.observeForever { updateMapCameraTargets() }
        _selectedPlace.observeForever { updateMapCameraTargets() }
        // This is the place displayed on screen, which should cause address search
        selectedPlace.observeForever {
            it?.run {
                searchSelectedPlace(latitude, longitude)
            }
        }
        _mapCameraMode.observeForever { updateMapCameraTargets() }
    }

    fun selectPlace(latitude: Double, longitude: Double, suggestedName: String? = null) {
        _selectedPlace.value = Place(latitude, longitude)
        searchSelectedPlace(latitude, longitude, suggestedName)
    }

    private fun searchSelectedPlace(latitude: Double, longitude: Double, suggestedName: String? = null) {
        launch(UI) {
            try {
                _selectedPlaceInfo.value = withContext(CommonPool) {
                    TencentLbsApi.searchPlaceInfo(Place(latitude, longitude), suggestedName)
                }
            } catch (e: Exception) {
                // TODO: Show error in UI
                println("Error: ${e.message}")
            }
        }
    }

    fun deselectPlace() {
        _selectedPlace.value = null
    }

    fun toggleEnableNotificationForSelectedPlace() {
        if (_selectedPlace.value != null) {
            val enable = !_enableNotificationForSelectedPlace.value!!
            _enableNotificationForSelectedPlace.value = enable
            if (enable) {
                PlaceNotifier.setDestination(_selectedPlace.value)
                _mapCameraMode.value = MapCameraMode.CENTER_TERMINALS
                enableNotification()
            } else {
                if (_selectedPlace.value == null) {
                    _selectedPlace.value = destination.value
                }
                PlaceNotifier.setDestination(null)
                disableNotification()
            }
        }
    }

    fun activityCreate() {
        permissionsList.forEach {
            if (ContextCompat.checkSelfPermission(App.context, it) == PackageManager.PERMISSION_GRANTED)
                _grantedPermissions.value = (_grantedPermissions.value?: emptySet()) + it
        }
        // Restore camera
        PreferencesRepo.loadMyLocation()?.let {
            _myLocationCache.value = it
        }
    }

    fun activityDestroy() {
        // Save current location for next startup
        PreferencesRepo.saveMyLocation(myLocation.value)
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
        val des = destination.value
        if (des != null) {
            App.context.run {
                ContextCompat.startForegroundService(this, Intent(this, TrackingService::class.java).apply {
                    action = TrackingService.ACTION_START_TRACKING
                    putExtra(TrackingService.EXTRA_DESTINATION_LAT, des.latitude)
                    putExtra(TrackingService.EXTRA_DESTINATION_LNG, des.longitude)
                })
            }
        }
    }

    private fun disableNotification() {
        App.context.sendBroadcast(Intent(TrackingService.ACTION_STOP_TRACKING))
    }

    private fun updateMapCameraTargets() {
        val mode = _mapCameraMode.value?: MapCameraMode.FREE
        val myLocation = this.myLocation.value
        val selectedPlace = this._selectedPlace.value
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
