package com.perqin.letmego.data.api

import android.net.Uri
import com.perqin.letmego.App
import com.perqin.letmego.BuildConfig
import com.perqin.letmego.R
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.placeinfo.PlaceInfo
import org.json.JSONObject
import java.net.URL

/**
 * Created by perqin on 2018/08/03.
 */
object TencentLbsApi {
    private const val baseUrl = "https://apis.map.qq.com/ws"
    private const val key = BuildConfig.TENCENT_LBS_KEY

    fun searchPlaceInfo(place: Place, suggestedName: String?): PlaceInfo {
        val json = URL("$baseUrl/geocoder/v1/?location=${place.latitude},${place.longitude}&key=$key").readText()
        val obj = JSONObject(json)
        val status = obj.getInt("status")
        val message = obj.getString("message")
        if (status != 0) {
            throw RuntimeException("Tencent LBS API: status = $status, message = $message")
        }
        val title = suggestedName?: obj.getJSONObject("result").optJSONObject("formatted_addresses")?.getString("recommend")?: App.context.getString(R.string.point_on_map)
        val address = obj.getJSONObject("result").getString("address")
        return PlaceInfo(title, address)
    }

    fun searchPlace(query: String, latitude: Double, longitude: Double): Place? {
        val uri = Uri.parse("$baseUrl/place/v1/search?key=$key").buildUpon()
                .appendQueryParameter("keyword", query)
                .appendQueryParameter("boundary", "nearby($latitude,$longitude,1000)")
                .build()
        val json = URL(uri.toString()).readText()
        val obj = JSONObject(json)
        val status = obj.getInt("status")
        val message = obj.getString("message")
        if (status != 0) {
            throw RuntimeException("Tencent LBS API: status = $status, message = $message")
        }
        if (obj.getInt("count") == 0) {
            return null
        }
        val data = obj.getJSONArray("data").getJSONObject(0)
        val resultLatitude = data.getJSONObject("location").getDouble("lat")
        val resultLongitude = data.getJSONObject("location").getDouble("lng")
        val title = data.getString("title")
        return Place(resultLatitude, resultLongitude, title)
    }
}
