package com.perqin.letmego.pages.main

import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.perqin.letmego.data.location.TencentLocator
import com.perqin.letmego.data.place.Place

class MainFragmentViewModel : ViewModel() {
    val myLocation = Transformations.map(TencentLocator.getLocation()) {
        Place(it.latitude, it.longitude)
    }
}
