package com.perqin.letmego.data

import androidx.room.Room

/**
 * Created by perqinxie on 2018/07/20.
 */
object class DestinationRepo {
    val dao = Room.databaseBuilder(null, AppDatabase::class.java, "app").build().destinationDao()
}
