package com.perqin.letmego.pages.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.LocationSource
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.MyLocationStyle
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.main_fragment.*

class MainFragment : Fragment() {
    private lateinit var locationSource: LocationSource
    private lateinit var aMap: AMap
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: MainViewModel
    private lateinit var mapView2: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    /**
     * This can be regarded as activity.onCreate
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mapView2 = mapView

        mapView.onCreate(savedInstanceState)
        locationSource = AMapLocationSource(context!!)
        aMap = mapView.map
        aMap.setLocationSource(locationSource)
        aMap.setMyLocationStyle(MyLocationStyle().apply {
            myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
        })
        aMap.isMyLocationEnabled = true
        aMap.setOnMapClickListener {
            activityViewModel.deselectPlace()
        }
        aMap.setOnMapLongClickListener {
            activityViewModel.selectPlace(it)
        }

        activityViewModel = ViewModelProviders.of(activity!!).get(MainActivityViewModel::class.java)
        activityViewModel.selectedPlace.observe(this, Observer {
        })

        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView2.onDestroy()
        locationSource.deactivate()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance() = MainFragment()
    }

    class AMapLocationSource(private val context: Context) : LocationSource {
        private var listener: LocationSource.OnLocationChangedListener? = null

        private var locationClient: AMapLocationClient? = null

        override fun activate(listener: LocationSource.OnLocationChangedListener?) {
            this.listener = listener
            if (locationClient == null) {
                locationClient = AMapLocationClient(context.applicationContext).apply {
                    setLocationListener {
                        println("onLocationChanged: ${it.latitude}, ${it.longitude}; err = ${it.errorCode}, ${it.errorInfo}")
                        listener?.onLocationChanged(it)
                    }
                    setLocationOption(AMapLocationClientOption().apply {
                        isLocationCacheEnable = false
                    })
                    startLocation()
                }
            }
        }

        override fun deactivate() {
            this.listener = null
            locationClient?.stopLocation()
            locationClient?.onDestroy()
            locationClient = null
        }
    }
}
