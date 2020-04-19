package com.perqin.letmego.data.destination

import androidx.lifecycle.LiveData
import androidx.room.*

/**
 * Created by perqinxie on 2018/07/20.
 * TODO: Rename to FavoriteDestinationDao
 */
@Dao
interface DestinationDao {
    @Query("SELECT * FROM destination")
    fun getAllLiveDestinations(): LiveData<List<Destination>>

    @Query("SELECT COUNT(id) FROM destination WHERE (ABS(latitude - :latitude) <= ${Destination.MINIMAL_DEGREE}) AND (ABS(longitude - :longitude) <= ${Destination.MINIMAL_DEGREE})")
    fun countLiveDestination(latitude: Double, longitude: Double): LiveData<Int>

    @Query("SELECT * FROM destination WHERE (ABS(latitude - :latitude) <= ${Destination.MINIMAL_DEGREE}) AND (ABS(longitude - :longitude) <= ${Destination.MINIMAL_DEGREE})")
    suspend fun getDestinationsAt(latitude: Double, longitude: Double): List<Destination>

    @Query("SELECT * FROM destination WHERE id = :id")
    suspend fun getDestinationById(id: Long): Destination

    @Insert
    suspend fun add(vararg destination: Destination)

    @Delete
    suspend fun remove(vararg destination: Destination)

    @Update
    suspend fun updateRemarkOfDestination(vararg destination: Destination)

    @Delete
    suspend fun deleteDestination(vararg destination: Destination)
}
