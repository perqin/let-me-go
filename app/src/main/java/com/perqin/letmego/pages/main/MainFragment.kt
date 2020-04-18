package com.perqin.letmego.pages.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.SearchManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.*
import android.widget.SearchView
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.services.TrackingService
import com.perqin.letmego.ui.destinationlist.DestinationListFragment
import com.perqin.letmego.utils.TencentMapGestureAdapter
import com.perqin.letmego.utils.createBitmapFromDrawableRes
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.model.*
import kotlinx.android.synthetic.main.layout_app_bar.*
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder) {
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
    private val viewModel: MainFragmentViewModel by viewModels()
    private var destinationMarker: Marker? = null
    private var destinationRangeCircle: Circle? = null
    private var selectedPlaceMarker: Marker? = null
    private var lockMapCamera = false
    private val mapOnClickBlocker = DelayBlocker()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Ensure that the SupportMapFragment is instantiated correctly
        childFragmentManager.fragmentFactory = object : FragmentFactory() {
            override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
                return if (SupportMapFragment::class.java.name == className) {
                    SupportMapFragment.newInstance(context)
                } else {
                    super.instantiate(classLoader, className)
                }
            }
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        tencentMap = (childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).map
        tencentMap.uiSettings.apply {
            setLogoPosition(TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT)
        }
        tencentMap.setOnMapClickListener {
            if (!mapOnClickBlocker.isBlocked()) {
                viewModel.deselectPlace()
            }
        }
        tencentMap.setOnMapLongClickListener {
            viewModel.selectPlace(it.latitude, it.longitude)
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
                viewModel.freeMapCamera()
                return false
            }

            override fun onFling(p0: Float, p1: Float): Boolean {
                viewModel.freeMapCamera()
                return false
            }

            override fun onLongPress(p0: Float, p1: Float): Boolean {
                viewModel.freeMapCamera()
                return false
            }

            override fun onScroll(p0: Float, p1: Float): Boolean {
                viewModel.freeMapCamera()
                return false
            }

            override fun onSingleTap(p0: Float, p1: Float): Boolean {
                viewModel.freeMapCamera()
                return false
            }
        })
        tencentMap.setOnMapPoiClickListener {
            // Block for a short time to avoid map's onClickListener being called after this listener is called.
            // This is due to the wrong design of Tencent LBS SDK.
            mapOnClickBlocker.block()
            viewModel.selectPlace(it.position.latitude, it.position.longitude, it.name)
        }

        @Suppress("DEPRECATION")
        val markerOptions = MarkerOptions()
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(
                createBitmapFromDrawableRes(requireContext(), R.drawable.my_location_marker)))
        markerOptions.zIndex(Z_INDEX_MY_LOCATION)
        myLocationMarker = tencentMap.addMarker(markerOptions)

        viewModel.detailedPlaceInfo.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showDetail(it.title, it.address)
            } else {
                hideDetail()
            }
        })
        viewModel.cameraStatus.observe(viewLifecycleOwner, Observer {
            when(it!!.mode) {
                MainFragmentViewModel.MapCameraMode.FREE -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_my_location_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.tint_inactivate))
                }
                MainFragmentViewModel.MapCameraMode.CENTER_MY_LOCATION -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_my_location_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.tint_activate))
                }
                MainFragmentViewModel.MapCameraMode.CENTER_TERMINALS -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_center_focus_strong_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.tint_activate))
                }
                else -> {}
            }
        })
        viewModel.isFavoriteForSelectedPlace.observe(viewLifecycleOwner, Observer {
            favoriteImageButton.setImageResource(if (it)
                R.drawable.ic_baseline_star_24
            else
                R.drawable.ic_star_border_black_24dp)
        })
        viewModel.enableNotificationForSelectedPlace.observe(viewLifecycleOwner, Observer {
            notifyImageButton.setImageResource(
                    if (it)
                        R.drawable.ic_notifications_active_black_24dp
                    else
                        R.drawable.ic_notifications_none_black_24dp
            )
            notifyImageButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),
                    if (it)
                        R.color.colorPrimary
                    else
                        R.color.black
            ))
        })

        mapCameraModeFab.setOnClickListener {
            viewModel.rotateMapCameraMode()
        }
        favoriteListFab.setOnClickListener {
            DestinationListFragment.newInstance().show(requireActivity().supportFragmentManager, null)
        }
        favoriteImageButton.setOnClickListener {
            viewModel.toggleFavoriteForDetailedPlace()
        }
        notifyImageButton.setOnClickListener {
            viewModel.toggleEnableNotificationForDetailedPlace()
        }

        viewModel.myLocation.observe(viewLifecycleOwner, Observer {
            myLocationMarker.position = LatLng(it.latitude, it.longitude)
        })
        viewModel.destination.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showDestinationMarker(it)
            } else {
                hideDestinationMarker()
            }
        })
        viewModel.selectedPlace.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                showSelectedPlaceMarker(it)
            } else {
                hideSelectedPlaceMarker()
            }
        })
        viewModel.cameraStatus.observe(viewLifecycleOwner, Observer {
            if (!lockMapCamera) {
                if (it.mode == MainFragmentViewModel.MapCameraMode.CENTER_MY_LOCATION && it.targets.isNotEmpty()) {
                    // Center it
                    tencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            LatLng(it.targets[0].latitude, it.targets[0].longitude),
                            15.0F
                    ))
                } else if (it.mode == MainFragmentViewModel.MapCameraMode.CENTER_TERMINALS && it.targets.size > 1) {
                    // Zoom to include all places
                    val padding = resources.getDimensionPixelSize(R.dimen.map_camera_padding)
                    tencentMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(
                            LatLngBounds.builder().apply {
                                it.targets.forEach { target ->
                                    include(LatLng(target.latitude, target.longitude))
                                }
                            }.build(),
                            padding, padding, padding, padding
                    ))
                }
            }
        })

        if (trackingService == null) {
            requireContext().run {
                bindService(Intent(this, TrackingService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
            }
        }

        viewModel.activityCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingService?.uiNotRequireMyLocationUpdates()
        requireContext().unbindService(serviceConnection)
        viewModel.activityDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.action_map, menu)
        // Associate searchable configuration with the SearchView
        val searchManager = requireContext().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.searchItem).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isQueryRefinementEnabled = true
        }
    }

    private fun showDetail(title: String, address: String) {
        placeTitleTextView.text = title
        placeAddressTextView.text = address
        if (placeDetailConstraintLayout.visibility != View.VISIBLE) {
            placeDetailConstraintLayout.visibility = View.VISIBLE
            ObjectAnimator
                    .ofFloat(placeDetailConstraintLayout, "translationY", 0F)
                    .apply {
                        duration = 500
                    }
                    .start()
        }
    }

    private fun hideDetail() {
        ObjectAnimator
                .ofFloat(placeDetailConstraintLayout, "translationY", placeDetailConstraintLayout.height.toFloat())
                .apply {
                    duration = 500
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            placeDetailConstraintLayout.visibility = View.GONE
                        }
                    })
                }
                .start()
    }

    private fun showSelectedPlaceMarker(place: Place) {
        val latLng = LatLng(place.latitude, place.longitude)
        if (selectedPlaceMarker == null) {
            selectedPlaceMarker = tencentMap.addMarker(MarkerOptions(latLng).apply {
                icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawableRes(requireContext(), R.drawable.ic_place_black_24dp)))
                anchor(0.5F, 1.0F)
                zIndex(Z_INDEX_SELECTED)
            })
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
                icon(BitmapDescriptorFactory.fromBitmap(createBitmapFromDrawableRes(requireContext(), R.drawable.destination_marker)))
                zIndex(Z_INDEX_DESTINATION)
            })
        } else {
            destinationMarker!!.position = latLng
        }
        if (destinationRangeCircle == null) {
            destinationRangeCircle = tencentMap.addCircle(CircleOptions().apply {
                center(latLng)
                radius(500.0)
                strokeWidth(0F)
                fillColor(Color.parseColor("#7F009688"))
                zIndex(Z_INDEX_DESTINATION_RANGE)
            })
        } else {
            destinationRangeCircle!!.center = latLng
        }
    }

    private fun hideDestinationMarker() {
        destinationMarker?.remove()
        destinationMarker = null
        destinationRangeCircle?.remove()
        destinationRangeCircle = null
    }

    fun selectDestination(destination: Destination) {
        viewModel.selectPlace(destination.latitude, destination.longitude, destination.displayName)
    }

    fun searchDestination(query: String) {
        viewModel.searchDestination(query)
    }

    companion object {
        fun newInstance() = MainFragment()
        private const val Z_INDEX_DESTINATION_RANGE = 1
        private const val Z_INDEX_DESTINATION = 2F
        private const val Z_INDEX_MY_LOCATION = 10F
        private const val Z_INDEX_SELECTED = 20F
    }

    @UiThread
    private class DelayBlocker(private var blocked: Boolean = false, val delay: Long = 500L) {
        private val handler = Handler(Looper.getMainLooper())
        private val runnable = {
            blocked = false
        }

        fun block() {
            blocked = true
            handler.removeCallbacks(runnable)
            handler.postDelayed(runnable, delay)
        }

        fun isBlocked() = blocked
    }
}
