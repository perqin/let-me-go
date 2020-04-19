package com.perqin.letmego.pages.destinationlist

import androidx.lifecycle.ViewModel
import com.perqin.letmego.data.destination.Destination
import com.perqin.letmego.data.destination.DestinationRepo

class DestinationListViewModel : ViewModel() {
    val destinations = DestinationRepo.getAllLiveDestinations()

    suspend fun updateRemarkOfDestination(destination: Destination, newRemark: String) {
        DestinationRepo.updateRemarkOfDestination(destination, newRemark)
    }

    suspend fun deleteDestination(destination: Destination) {
        DestinationRepo.deleteDestination(destination)
    }
}
