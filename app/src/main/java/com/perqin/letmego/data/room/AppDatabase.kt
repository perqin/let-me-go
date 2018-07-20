package com.perqin.letmego.data.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.perqin.letmego.App
import com.perqin.letmego.data.destination.Destination
import com.perqin.letmego.data.destination.DestinationDao

/**
 * Created by perqinxie on 2018/07/20.
 */
@Database(entities = [Destination::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun destinationDao(): DestinationDao
}

val appDatabase = Room.databaseBuilder(App.context, AppDatabase::class.java, "app").build()
