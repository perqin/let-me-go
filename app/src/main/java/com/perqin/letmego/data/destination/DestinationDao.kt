package com.perqin.letmego.data.destination

import androidx.room.Dao
import androidx.room.Query

/**
 * Created by perqinxie on 2018/07/20.
 */
@Dao
interface DestinationDao {
    @Query("SELECT * FROM destination")
    fun getAllDestinations(): List<Destination>
}
