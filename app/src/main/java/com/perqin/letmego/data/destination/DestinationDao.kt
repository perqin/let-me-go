package com.perqin.letmego.data.destination

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query

/**
 * Created by perqinxie on 2018/07/20.
 * TODO: Rename to FavoriteDestinationDao
 */
@Dao
interface DestinationDao {
    @Query("SELECT * FROM destination")
    fun getAllLiveDestinations(): LiveData<List<Destination>>
}
