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
import com.tencent.tencentmap.mapsdk.maps.SupportMapFragment
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions

class MainFragment : Fragment() {
    private lateinit var tencentMap: TencentMap
    private lateinit var myLocationMarker: Marker
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: MainFragmentViewModel
    private var defaultMarker: Marker? = null

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
        activityViewModel.selectedPlace.observe(this, Observer {
            if (it != null) {
                showDefaultMarker(it)
            } else {
                hideDefaultMarker()
            }
        })

        viewModel = ViewModelProviders.of(this).get(MainFragmentViewModel::class.java)
        viewModel.myLocation.observe(this, Observer {
            myLocationMarker.position = LatLng(it.latitude, it.longitude)
        })
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
