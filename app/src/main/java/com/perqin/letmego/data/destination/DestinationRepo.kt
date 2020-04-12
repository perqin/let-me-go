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
    val dao = appDatabase.destinationDao()

    fun isDestinationExisting(place: Place): LiveData<Boolean> {
        return Transformations.map(dao.countLiveDestination(place.latitude, place.longitude)) {
            it == 1
        }
    }

    suspend fun add(place: Place) {
        val displayName = place.suggestedName?:App.context.getString(R.string.point_on_map)
        dao.add(Destination(null, place.latitude, place.longitude, displayName))
    }

    suspend fun remove(place: Place) {
        dao.remove(*dao.getDestinationsAt(place.latitude, place.longitude).toTypedArray())
    }
}
