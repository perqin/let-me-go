package com.perqin.letmego.pages.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.perqin.letmego.R
import com.perqin.letmego.pages.main.permissions.PermissionsFragment
import com.perqin.letmego.utils.permissionsAllGranted

class MainActivity : AppCompatActivity(), PermissionsFragment.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        val fragment = if (!permissionsAllGranted()) {
            PermissionsFragment.newInstance()
        } else {
            MainFragment.newInstance()
        }
        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, fragment)
                .commit()
    }

    override fun onAllPermissionsGranted() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MainFragment.newInstance())
                .commit()
    }
}
