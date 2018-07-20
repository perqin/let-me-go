package com.perqin.letmego.ui.destinationlist

import androidx.lifecycle.ViewModel
import com.perqin.letmego.data.destination.DestinationRepo

class DestinationListViewModel : ViewModel() {
    val destinations = DestinationRepo.dao.getAllLiveDestinations()
}
