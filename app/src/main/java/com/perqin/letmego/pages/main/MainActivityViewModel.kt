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
            value = userSelectedPlace?: destination
        }
    }

    private val _selectedPlaceInfo = MutableLiveData<PlaceInfo?>()
    val selectedPlaceInfo: LiveData<PlaceInfo?> = _selectedPlaceInfo

//    private val _enableNotificationForSelectedPlace = MutableLiveData<Boolean>()
    val enableNotificationForSelectedPlace = object : LiveData<Boolean>() {
        private var selectedPlace: Place? = null
        private var destination: Place? = null

        init {
            this@MainActivityViewModel.selectedPlace.observeForever {
                this.selectedPlace = it
                updateValue()
            }
            this@MainActivityViewModel.destination.observeForever {
                this.destination = it
                updateValue()
            }
        }

        private fun updateValue() {
            value = selectedPlace != null && destination != null &&
                    selectedPlace!!.latitude == destination!!.latitude &&
                    selectedPlace!!.longitude == destination!!.longitude
        }
    }

//    private val _mapCameraMode = MutableLiveData<MapCameraMode>()
//    val mapCameraMode: LiveData<MapCameraMode> = Transformations.map(_mapCameraMode) {
//        if (it == MapCameraMode.AUTO) {
//            when {
//                destination.value != null -> MapCameraMode.CENTER_TERMINALS
//                myLocation.value != null -> MapCameraMode.CENTER_MY_LOCATION
//                else -> MapCameraMode.FREE
//            }
//        } else {
//            it
//        }
//    }
//
//    private val _mapCameraTargets = MutableLiveData<List<Place>>()
//    val mapCameraTargets: LiveData<List<Place>> = _mapCameraTargets
    private val _cameraMode = MutableLiveData<MapCameraMode>()
    private val _cameraStatus = MutableLiveData<CameraStatus>()
    val cameraStatus: LiveData<CameraStatus> = _cameraStatus

    private val _grantedPermissions = MutableLiveData<Set<String>>()
    val grantedPermissions: LiveData<Set<String>> = _grantedPermissions

    val allPermissionsGranted: LiveData<Boolean> = Transformations.map(_grantedPermissions) {
        it.containsAll(permissionsList)
    }

    init {
//        _enableNotificationForSelectedPlace.value = false
//        _mapCameraMode.value = MapCameraMode.AUTO
//        _mapCameraTargets.value = emptyList()
//        _cameraStatus.value = CameraStatus(MapCameraMode.AUTO, emptyList())
        _grantedPermissions.value = emptySet()
        _cameraMode.observeForever { updateMapCameraStatus() }
        myLocation.observeForever { updateMapCameraStatus() }
        destination.observeForever { updateMapCameraStatus() }
//        _selectedPlace.observeForever { updateMapCameraStatus() }
        // This is the place displayed on screen, which should cause address search
        selectedPlace.observeForever {
            it?.run {
                searchSelectedPlace(latitude, longitude)
            }
        }
//        _mapCameraMode.observeForever { updateMapCameraTargets() }
    }

    fun selectPlace(latitude: Double, longitude: Double, suggestedName: String? = null) {
        _selectedPlace.value = Place(latitude, longitude)
//        searchSelectedPlace(latitude, longitude, suggestedName)
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
        selectedPlace.value?.let {
            PlaceNotifier.setOrUnsetDestination(it)
        }
//        if (_selectedPlace.value != null) {
//            val enable = !_enableNotificationForSelectedPlace.value!!
//            _enableNotificationForSelectedPlace.value = enable
//            if (enable) {
//                PlaceNotifier.setDestination(_selectedPlace.value)
//                _cameraMode.value = MapCameraMode.CENTER_TERMINALS
//                enableNotification()
//            } else {
//                if (_selectedPlace.value == null) {
//                    _selectedPlace.value = destination.value
//                }
//                PlaceNotifier.setDestination(null)
//                disableNotification()
//            }
//        }
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
//        _mapCameraMode.value = when (_mapCameraMode.value?: MapCameraMode.FREE) {
//            MapCameraMode.FREE -> MapCameraMode.CENTER_MY_LOCATION
//            MapCameraMode.CENTER_MY_LOCATION ->
//                if (_selectedPlace.value != null)
//                    MapCameraMode.CENTER_TERMINALS
//                else
//                    MapCameraMode.FREE
//            MapCameraMode.CENTER_TERMINALS -> MapCameraMode.FREE
//        }
        val currentMode = cameraStatus.value?.mode
        if (currentMode == MapCameraMode.FREE) {
            _cameraMode.value = MapCameraMode.CENTER_MY_LOCATION
        } else if (currentMode == MapCameraMode.CENTER_MY_LOCATION) {
            _cameraMode.value = if (destination.value != null) {
                MapCameraMode.CENTER_TERMINALS
            } else {
                MapCameraMode.FREE
            }
        } else if (currentMode == MapCameraMode.CENTER_TERMINALS) {
            _cameraMode.value = MapCameraMode.FREE
        }
//        _cameraMode.value = when (currentMode) {
//            MapCameraMode.FREE -> MapCameraMode.CENTER_MY_LOCATION
//            MapCameraMode.CENTER_MY_LOCATION ->
//                if (_selectedPlace.value != null) {
//                    MapCameraMode.CENTER_TERMINALS
//                } else {
//                    MapCameraMode.FREE
//                }
//            MapCameraMode.CENTER_TERMINALS -> MapCameraMode.FREE
//            else -> MapCameraMode.FREE
//        }
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

//    private fun updateMapCameraTargets() {
//        val mode = _mapCameraMode.value?: MapCameraMode.FREE
//        val myLocation = this.myLocation.value
//        val selectedPlace = this._selectedPlace.value
//        _mapCameraTargets.value = if (myLocation != null) {
//            when (mode) {
//                MainActivityViewModel.MapCameraMode.FREE -> emptyList()
//                MainActivityViewModel.MapCameraMode.CENTER_MY_LOCATION -> listOf(myLocation)
//                MainActivityViewModel.MapCameraMode.CENTER_TERMINALS -> listOf(myLocation, selectedPlace!!)
//            }
//        } else {
//            emptyList()
//        }
//    }

    private fun updateMapCameraStatus() {
        val mode = _cameraMode.value?: MapCameraMode.AUTO
        val myLocation = myLocation.value
        val destination = destination.value
        _cameraStatus.value = when (mode) {
            MapCameraMode.AUTO -> {
                if (destination != null) {
                    CameraStatus.centerTerminals(listOf(myLocation, destination))
                } else {
                    CameraStatus.centerMyLocation(myLocation)
                }
            }
            MapCameraMode.FREE -> CameraStatus.free()
            MapCameraMode.CENTER_MY_LOCATION -> CameraStatus.centerMyLocation(myLocation)
            MapCameraMode.CENTER_TERMINALS -> CameraStatus.centerTerminals(listOf(myLocation, destination!!))
        }
    }

    fun freeMapCamera() {
//        if (_mapCameraMode.value != MapCameraMode.FREE) {
//            _mapCameraMode.value = MapCameraMode.FREE
//        }
        _cameraMode.value = MapCameraMode.FREE
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

    enum class MapCameraMode { AUTO, FREE, CENTER_MY_LOCATION, CENTER_TERMINALS }

    data class CameraStatus(
            val mode: MapCameraMode,
            val targets: List<Place> = emptyList()
    ) {
        companion object {
            fun free() = CameraStatus(MapCameraMode.FREE)
            fun centerMyLocation(myLocation: Place?) = CameraStatus(MapCameraMode.CENTER_MY_LOCATION, listOfNotNull(myLocation))
            fun centerTerminals(terminals: List<Place?>) = CameraStatus(MapCameraMode.CENTER_TERMINALS, terminals.filterNotNull())
        }
    }
}
