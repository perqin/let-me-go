package com.perqin.letmego.receiver

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.perqin.letmego.data.destination.DestinationRepo
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.place.PlaceNotifier
import com.twofortyfouram.locale.sdk.client.receiver.AbstractPluginSettingReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskerReceiver : AbstractPluginSettingReceiver() {
    override fun isAsync(): Boolean {
        return false
    }

    override fun firePluginSetting(context: Context, bundle: Bundle) {
        val destinationId = bundle.getLong(EXTRA_DESTINATION_ID)
        GlobalScope.launch(Dispatchers.Main) {
            try {
                val destination = DestinationRepo.getDestinationById(destinationId)
                PlaceNotifier.setOrUnsetDestination(Place(destination.latitude, destination.longitude))
            } catch (e: Exception) {
                Log.e(TAG, "firePluginSetting", e)
            }
        }
    }

    override fun isBundleValid(bundle: Bundle): Boolean {
        return bundle.containsKey(EXTRA_DESTINATION_ID)
    }

    companion object {
        const val EXTRA_DESTINATION_ID = "destinationId"
        private const val TAG = "TaskerReceiver"
    }
}
