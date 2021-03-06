package com.perqin.letmego.pages.main.map

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import com.perqin.letmego.App
import com.perqin.letmego.R
import com.perqin.letmego.data.api.TencentLbsApi
import com.perqin.letmego.data.destination.DestinationRepo
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier
import com.perqin.letmego.data.placeinfo.PlaceInfo
import com.perqin.letmego.data.preferences.PreferencesRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapFragmentViewModel(application: Application) : AndroidViewModel(application) {
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
    val selectedPlace: LiveData<Place?> = _selectedPlace

    // The info card may show information for pin selected place or destination
    private val _detailedPlace: LiveData<Place?> = object : MediatorLiveData<Place?>() {
        private var userSelectedPlace: Place? = null
        private var destination: Place? = null

        init {
            addSource(this@MapFragmentViewModel.destination) {
                this.destination = it
                updateValue()
            }
            addSource(this@MapFragmentViewModel._selectedPlace) {
                this.userSelectedPlace = it
                updateValue()
            }
        }

        private fun updateValue() {
            value = userSelectedPlace?: destination
        }
    }
    private val _detailedPlaceInfo = MutableLiveData<PlaceInfo?>()
    val detailedPlaceInfo: LiveData<PlaceInfo?> = _detailedPlaceInfo

    val isFavoriteForSelectedPlace = Transformations.switchMap(_detailedPlace) {
        if (it == null) {
            MutableLiveData<Boolean>().apply { value = false }
        } else {
            DestinationRepo.isDestinationExisting(it)
        }
    }

    val enableNotificationForSelectedPlace = object : LiveData<Boolean>() {
        private var detailedPlace: Place? = null
        private var destination: Place? = null

        init {
            this@MapFragmentViewModel._detailedPlace.observeForever {
                this.detailedPlace = it
                updateValue()
            }
            this@MapFragmentViewModel.destination.observeForever {
                this.destination = it
                updateValue()
            }
        }

        private fun updateValue() {
            value = detailedPlace != null && destination != null &&
                    Place.isEqual(detailedPlace!!, destination!!)
        }
    }

    private val _cameraMode = MutableLiveData<MapCameraMode>()
    private val _cameraStatus = MutableLiveData<CameraStatus>()
    val cameraStatus: LiveData<CameraStatus> = _cameraStatus

    init {
        destination.observeForever {
            if (it != null) {
                _cameraMode.value = MapCameraMode.CENTER_TERMINALS
            } else if (_cameraMode.value == MapCameraMode.CENTER_TERMINALS) {
                _cameraMode.value = MapCameraMode.FREE
            }
        }
        _cameraMode.observeForever { updateMapCameraStatus() }
        myLocation.observeForever { updateMapCameraStatus() }
        // This is the place displayed on screen, which should cause address search
        _detailedPlace.observeForever {
            if (it != null) {
                _detailedPlaceInfo.value = PlaceInfo(it.suggestedName?: App.context.getString(R.string.point_on_map), "")
                searchSelectedPlace(it.latitude, it.longitude, it.suggestedName)
            } else {
                _detailedPlaceInfo.value = null
            }
        }
    }

    fun selectPlace(latitude: Double, longitude: Double, suggestedName: String? = null) {
        _selectedPlace.value = Place(latitude, longitude, suggestedName)
    }

    private fun searchSelectedPlace(latitude: Double, longitude: Double, suggestedName: String? = null) {
        viewModelScope.launch {
            try {
                _detailedPlaceInfo.value = withContext(Dispatchers.IO) {
                    TencentLbsApi.searchPlaceInfo(Place(latitude, longitude), suggestedName)
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deselectPlace() {
        _selectedPlace.value = null
    }

    fun toggleFavoriteForDetailedPlace() {
        val detailedPlace = _detailedPlace.value?:return
        val address = _detailedPlaceInfo.value?.address.orEmpty()
        viewModelScope.launch {
            val isFavorite = isFavoriteForSelectedPlace.value?:false
            try {
                if (isFavorite) {
                    DestinationRepo.remove(detailedPlace)
                } else {
                    DestinationRepo.add(detailedPlace, address)
                }
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun toggleEnableNotificationForDetailedPlace() {
        _detailedPlace.value?.let {
            PlaceNotifier.setOrUnsetDestination(it)
        }
    }

    fun activityCreate() {
        // Restore camera
        PreferencesRepo.loadMyLocation()?.let {
            _myLocationCache.value = it
        }
    }

    fun activityDestroy() {
        // Save current location for next startup
        PreferencesRepo.saveMyLocation(myLocation.value)
        // Notice user
        if (destination.value != null) {
            App.context.run {
                Toast.makeText(this, getString(R.string.toast_app_is_still_tracking), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun rotateMapCameraMode() {
        when (cameraStatus.value?.mode) {
            MapCameraMode.FREE -> _cameraMode.value = MapCameraMode.CENTER_MY_LOCATION
            MapCameraMode.CENTER_MY_LOCATION -> _cameraMode.value = if (destination.value != null) {
                MapCameraMode.CENTER_TERMINALS
            } else {
                MapCameraMode.FREE
            }
            MapCameraMode.CENTER_TERMINALS -> _cameraMode.value = MapCameraMode.FREE
            else -> {}
        }
    }

    private fun updateMapCameraStatus() {
        val mode = _cameraMode.value?: MapCameraMode.CENTER_MY_LOCATION
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
        _cameraMode.value = MapCameraMode.FREE
    }

    fun searchDestination(query: String) {
        val place = myLocation.value?:Place(40.22077, 116.23128)
        viewModelScope.launch {
            try {
                val resultPlace = withContext(Dispatchers.IO) {
                    TencentLbsApi.searchPlace(query, place.latitude, place.longitude)
                }
                if (resultPlace == null) {
                    Toast.makeText(getApplication(), R.string.text_destinationNotFound, Toast.LENGTH_SHORT).show()
                    return@launch
                }
                selectPlace(resultPlace.latitude, resultPlace.longitude, resultPlace.suggestedName)
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.text_failToOperate, Toast.LENGTH_SHORT).show()
            }
        }
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
