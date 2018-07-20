package com.perqin.letmego

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.perqin.letmego.ui.destinationlist.DestinationListFragment

class DestinationListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.destination_list_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, DestinationListFragment.newInstance())
                    .commitNow()
        }
    }

}
