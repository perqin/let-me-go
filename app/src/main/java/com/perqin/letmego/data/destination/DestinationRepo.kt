package com.perqin.letmego.data.destination

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.perqin.letmego.App
import com.perqin.letmego.R
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.room.appDatabase

/**
 * Created by perqinxie on 2018/07/20.
 */
object DestinationRepo {
    private val dao = appDatabase.destinationDao()

    fun isDestinationExisting(place: Place): LiveData<Boolean> {
        return Transformations.map(dao.countLiveDestination(place.latitude, place.longitude)) {
            it == 1
        }
    }

    suspend fun add(place: Place, address: String) {
        val displayName = place.suggestedName?:App.context.getString(R.string.point_on_map)
        dao.add(Destination(null, place.latitude, place.longitude,
                Destination.COORDINATE_TENCENT, displayName, address))
    }

    suspend fun remove(place: Place) {
        dao.remove(*dao.getDestinationsAt(place.latitude, place.longitude).toTypedArray())
    }

    fun getAllLiveDestinations() = dao.getAllLiveDestinations()

    suspend fun updateRemarkOfDestination(destination: Destination, newRemark: String) {
        dao.updateRemarkOfDestination(Destination(destination.id, destination.latitude, destination.longitude,
                Destination.COORDINATE_TENCENT, newRemark, destination.address))
    }

    suspend fun deleteDestination(destination: Destination) {
        dao.deleteDestination(destination)
    }

    suspend fun getDestinationById(destinationId: Long): Destination {
        return dao.getDestinationById(destinationId)
    }
}
