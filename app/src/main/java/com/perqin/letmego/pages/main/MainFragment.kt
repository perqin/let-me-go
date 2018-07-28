package com.perqin.letmego.pages.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.perqin.letmego.R
import com.perqin.letmego.data.place.Place
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.LatLngBounds
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions

class MainFragment : Fragment() {
    private lateinit var tencentMap: TencentMap
    private lateinit var myLocationMarker: Marker
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: MainFragmentViewModel
    private var defaultMarker: Marker? = null
    private var mapCameraMode = MainActivityViewModel.MapCameraMode.FREE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    /**
     * This can be regarded as activity.onCreate
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        tencentMap = (childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment).map
        tencentMap.setOnMapClickListener {
            activityViewModel.deselectPlace()
        }
        tencentMap.setOnMapLongClickListener {
            activityViewModel.selectPlace(it.latitude, it.longitude)
        }

        @Suppress("DEPRECATION")
        myLocationMarker = tencentMap.addMarker(MarkerOptions())

        activityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        activityViewModel.mapCameraMode.observe(this, Observer {
            mapCameraMode = it
        })
        activityViewModel.myLocation.observe(this, Observer {
            myLocationMarker.position = LatLng(it.latitude, it.longitude)
        })
        activityViewModel.selectedPlace.observe(this, Observer {
            if (it != null) {
                showDefaultMarker(it)
            } else {
                hideDefaultMarker()
            }
        })
        activityViewModel.mapCameraTargets.observe(this, Observer { targets ->
            println("Update camera for targets: ${targets.joinToString { "(${it.latitude}, ${it.longitude})" }}")
            val padding = context!!.resources.getDimensionPixelSize(R.dimen.map_camera_padding)
            if (targets.size == 1) {
                // Center it
                tencentMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(targets[0].latitude, targets[0].longitude),
                        15.0F
                ))
            } else if (targets.size > 1) {
                // Zoom to include all places
                tencentMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(
                        LatLngBounds.builder().apply {
                            targets.forEach {
                                include(LatLng(it.latitude, it.longitude))
                            }
                        }.build(),
                        padding, padding, padding, padding
                ))
            }
        })

        viewModel = ViewModelProviders.of(this).get(MainFragmentViewModel::class.java)
    }

    private fun showDefaultMarker(place: Place) {
        val latLng = LatLng(place.latitude, place.longitude)
        if (defaultMarker == null) {
            defaultMarker = tencentMap.addMarker(MarkerOptions(latLng))
        } else {
            defaultMarker!!.position = latLng
        }
    }

    private fun hideDefaultMarker() {
        defaultMarker?.remove()
        defaultMarker = null
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}
