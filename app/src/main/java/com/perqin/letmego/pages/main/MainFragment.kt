package com.perqin.letmego.pages.main

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.perqin.letmego.R
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.services.TrackingService
import com.perqin.letmego.utils.TencentMapGestureAdapter
import com.perqin.letmego.utils.createBitmapFromDrawableRes
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.model.*
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
            println("Connect!!")
            trackingService = (service as TrackingService.LocalBinder).apply {
                uiRequireMyLocationUpdates()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            trackingService = null
        }
    }
    private var trackingService: TrackingService.LocalBinder? = null

    private lateinit var tencentMap: TencentMap
    private lateinit var myLocationMarker: Marker
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: MainFragmentViewModel
    private var destinationMarker: Marker? = null
    private var destinationRangeCircle: Circle? = null
    private var selectedPlaceMarker: Marker? = null
    private var lockMapCamera: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    /**
     * This can be regarded as activity.onCreate
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        grantPermissionsButton.setOnClickListener {
            var array = emptyArray<String>()
            MainActivityViewModel.permissionsList.forEach { array += it }
            requestPermissions(array, REQUEST_ALL_PERMISSIONS)
        }

        tencentMap = (childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).map
        tencentMap.uiSettings.apply {
            setLogoPosition(TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT)
        }
        tencentMap.setOnMapClickListener {
            activityViewModel.deselectPlace()
        }
        tencentMap.setOnMapLongClickListener {
            activityViewModel.selectPlace(it.latitude, it.longitude)
        }
        tencentMap.addTencentMapGestureListener(object : TencentMapGestureAdapter() {
            override fun onDown(p0: Float, p1: Float): Boolean {
                lockMapCamera = true
                return false
            }

            override fun onUp(p0: Float, p1: Float): Boolean {
                lockMapCamera = false
                return false
            }

            override fun onDoubleTap(p0: Float, p1: Float): Boolean {
                activityViewModel.freeMapCamera()
                return false
            }

            override fun onFling(p0: Float, p1: Float): Boolean {
                activityViewModel.freeMapCamera()
                return false
            }

            override fun onLongPress(p0: Float, p1: Float): Boolean {
                activityViewModel.freeMapCamera()
                return false
            }

            override fun onScroll(p0: Float, p1: Float): Boolean {
                activityViewModel.freeMapCamera()
                return false
            }

            override fun onSingleTap(p0: Float, p1: Float): Boolean {
                activityViewModel.freeMapCamera()
                return false
            }
        })
        tencentMap.setOnMapPoiClickListener {
            activityViewModel.selectPlace(it.position.latitude, it.position.longitude, it.name)
        }

        @Suppress("DEPRECATION")
        myLocationMarker = tencentMap.addMarker(MarkerOptions().apply {
            icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawableRes(context!!, R.drawable.my_location_marker)))
        })

        activityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        activityViewModel.myLocation.observe(this, Observer {
            myLocationMarker.position = LatLng(it.latitude, it.longitude)
        })
        activityViewModel.destination.observe(this, Observer {
            if (it != null) {
                showDestinationMarker(it)
            } else {
                hideDestinationMarker()
            }
        })
        activityViewModel.selectedPlace.observe(this, Observer {
            if (it != null) {
                showSelectedPlaceMarker(it)
            } else {
                hideSelectedPlaceMarker()
            }
        })
        activityViewModel.cameraStatus.observe(this, Observer {
            if (!lockMapCamera) {
                if (it.mode == MainActivityViewModel.MapCameraMode.CENTER_MY_LOCATION && it.targets.isNotEmpty()) {
                    // Center it
                    tencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.targets[0].latitude, it.targets[0].longitude),
                            15.0F
                    ))
                } else if (it.mode == MainActivityViewModel.MapCameraMode.CENTER_TERMINALS && it.targets.size > 1) {
                    // Zoom to include all places
                    val padding = context!!.resources.getDimensionPixelSize(R.dimen.map_camera_padding)
                    tencentMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(
                            LatLngBounds.builder().apply {
                                it.targets.forEach {
                                    include(LatLng(it.latitude, it.longitude))
                                }
                            }.build(),
                            padding, padding, padding, padding
                    ))
                }
            }
        })
        activityViewModel.allPermissionsGranted.observe(this, Observer {
            println("MainFragment: $it")
            permissionsGuideConstraintLayout.visibility = if (it) View.GONE else View.VISIBLE
            if (it && trackingService == null) {
                context!!.run {
                    bindService(Intent(this, TrackingService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
                }
            }
        })
        activityViewModel.grantedPermissions.observe(this, Observer {
            val red = ContextCompat.getColor(context!!, R.color.red_500)
            val green = ContextCompat.getColor(context!!, R.color.green_500)
            phonePermissionTextView.setTextColor(
                    if (it.containsAll(listOf(Manifest.permission.READ_PHONE_STATE)))
                        green
                    else
                        red
            )
            locationPermissionTextView.setTextColor(
                    if (it.containsAll(listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)))
                        green
                    else
                        red
            )
            storagePermissionTextView.setTextColor(
                    if (it.containsAll(listOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)))
                        green
                    else
                        red
            )
        })

        viewModel = ViewModelProviders.of(this).get(MainFragmentViewModel::class.java)
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingService?.uiNotRequireMyLocationUpdates()
        context!!.unbindService(serviceConnection)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_ALL_PERMISSIONS) {
            for (i in 0 until permissions.size) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    activityViewModel.permissionsGranted(permissions[i])
                }
            }
        }
    }

    private fun showSelectedPlaceMarker(place: Place) {
        val latLng = LatLng(place.latitude, place.longitude)
        if (selectedPlaceMarker == null) {
            selectedPlaceMarker = tencentMap.addMarker(MarkerOptions(latLng))
        } else {
            selectedPlaceMarker!!.position = latLng
        }
    }

    private fun hideSelectedPlaceMarker() {
        selectedPlaceMarker?.remove()
        selectedPlaceMarker = null
    }

    private fun showDestinationMarker(place: Place) {
        val latLng = LatLng(place.latitude, place.longitude)
        if (destinationMarker == null) {
            destinationMarker = tencentMap.addMarker(MarkerOptions(latLng).apply {
                icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawableRes(context!!, R.drawable.destination_marker)))
            })
        } else {
            destinationMarker!!.position = latLng
        }
        if (destinationRangeCircle == null) {
            destinationRangeCircle = tencentMap.addCircle(
                    CircleOptions()
                            .center(latLng)
                            .radius(500.0)
                            .strokeWidth(0F)
                            .fillColor(Color.parseColor("#7F009688"))
            )
        } else {
            destinationRangeCircle!!.center = latLng
        }
    }

    private fun hideDestinationMarker() {
        destinationMarker?.remove()
        destinationMarker = null
    }

    companion object {
        fun newInstance() = MainFragment()

        private const val REQUEST_ALL_PERMISSIONS = 1
    }
}
