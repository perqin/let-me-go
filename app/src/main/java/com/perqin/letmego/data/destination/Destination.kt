package com.perqin.letmego.data.destination

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Created by perqinxie on 2018/07/20.
 */
@Entity
class Destination(
        @PrimaryKey(autoGenerate = true)
        var id: Long?,
        @ColumnInfo
        var latitude: Double,
        @ColumnInfo
        var longitude: Double,
        @ColumnInfo
        var coordinateType: String,
        @ColumnInfo
        var displayName: String,
        @ColumnInfo
        var address: String
) {
    companion object {
        /**
         * Minimal degree. If two Destinations' distances for both latitude and longitude are smaller
         * than this value, they can be considered the same Destination.
         * 0.00001° is about 1.113195m on equator.
         */
        const val MINIMAL_DEGREE = 0.00001

        const val COORDINATE_TENCENT = "tencent"
    }
}
