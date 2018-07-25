package com.perqin.letmego.pages.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.MapView
import com.amap.api.maps2d.model.MyLocationStyle
import com.perqin.letmego.R

class MainFragment : Fragment() {
    private lateinit var aMap: AMap
    private lateinit var activityViewModel: MainActivityViewModel
    private lateinit var viewModel: MainViewModel
    private lateinit var mapView: MapView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Must get it explicitly, because Kotlin extension is not accessible in onDestroy
        mapView = view.findViewById(R.id.mapView)
    }

    /**
     * This can be regarded as activity.onCreate
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
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
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    companion object {
        fun newInstance() = MainFragment()
    }
}