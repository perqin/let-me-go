package com.perqin.letmego.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.perqin.letmego.App
import com.perqin.letmego.data.place.PlaceNotifier

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP_TRACKING -> {
                PlaceNotifier.setOrUnsetDestination(null)
            }
        }
    }

    companion object {
        val ACTION_STOP_TRACKING = "${App.context.packageName}.ACTION_STOP_TRACKING"
    }
}
