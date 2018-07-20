package com.perqin.letmego.data.destination

import com.perqin.letmego.data.room.appDatabase

/**
 * Created by perqinxie on 2018/07/20.
 */
object DestinationRepo {
    val dao = appDatabase.destinationDao()
}
