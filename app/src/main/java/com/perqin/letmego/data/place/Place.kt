package com.perqin.letmego.data.place

/**
 * @author perqin
 */
class Place(
        var latitude: Double,
        var longitude: Double,
        var suggestedName: String? = null
) {
    companion object {
        fun isEqual(a: Place, b: Place): Boolean =
                a.latitude == b.latitude && a.longitude == b.longitude
    }
}
