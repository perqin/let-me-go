package com.perqin.letmego.data.api

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

    fun searchPlaceInfo(place: Place): PlaceInfo {
        val json = URL("$baseUrl/geocoder/v1/?location=${place.latitude},${place.longitude}&key=$key").readText()
        val obj = JSONObject(json)
        val status = obj.getInt("status")
        val message = obj.getString("message")
        if (status != 0) {
            throw RuntimeException("Tencent LBS API: status = $status, message = $message")
        }
        val title = obj.getJSONObject("result").optJSONObject("formatted_addresses")?.getString("recommend")?: App.context.getString(R.string.point_on_map)
        val address = obj.getJSONObject("result").getString("address")
        return PlaceInfo(title, address)
    }
}
