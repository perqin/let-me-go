package com.perqin.letmego.pages.tasker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination
import com.perqin.letmego.data.tasker.TaskerReceiver
import com.perqin.letmego.ui.destinationlist.DestinationListFragment
import kotlinx.android.synthetic.main.layout_app_bar.*
import com.twofortyfouram.locale.api.Intent as TaskerIntent

class TaskerEditActivity : AppCompatActivity(), DestinationListFragment.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_activity)

        setSupportActionBar(toolbar)

        supportActionBar?.apply {
            title = getString(R.string.label_pickDestination)
        }

        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, DestinationListFragment.newInstance())
                .commit()
    }

    override fun onSelectDestination(destination: Destination) {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(TaskerIntent.EXTRA_STRING_BLURB, destination.displayName)
            putExtra(TaskerIntent.EXTRA_BUNDLE, Bundle().apply {
                putExtra(TaskerReceiver.EXTRA_DESTINATION_ID, destination.id)
            })
        })
        finish()
    }
}