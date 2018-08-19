package com.perqin.letmego.pages.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        toolbar.title = getString(R.string.app_name)
        setSupportActionBar(toolbar)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.detailedPlaceInfo.observe(this, Observer {
            if (it != null) {
                showDetail(it.title, it.address)
            } else {
                hideDetail()
            }
        })
        viewModel.cameraStatus.observe(this, Observer {
            when(it!!.mode) {
                MainActivityViewModel.MapCameraMode.FREE -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_my_location_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tint_inactivate))
                }
                MainActivityViewModel.MapCameraMode.CENTER_MY_LOCATION -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_my_location_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tint_activate))
                }
                MainActivityViewModel.MapCameraMode.CENTER_TERMINALS -> {
                    mapCameraModeFab.setImageResource(R.drawable.ic_center_focus_strong_black_24dp)
                    mapCameraModeFab.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.tint_activate))
                }
                else -> {}
            }
        })
        viewModel.enableNotificationForSelectedPlace.observe(this, Observer {
            notifyImageButton.setImageResource(
                    if (it)
                        R.drawable.ic_notifications_active_black_24dp
                    else
                        R.drawable.ic_notifications_none_black_24dp
            )
            notifyImageButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(this,
                    if (it)
                        R.color.colorPrimary
                    else
                        R.color.black
            ))
        })

        mapCameraModeFab.setOnClickListener {
            viewModel.rotateMapCameraMode()
        }

        notifyImageButton.setOnClickListener {
            viewModel.toggleEnableNotificationForDetailedPlace()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }

        viewModel.activityCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.activityDestroy()
    }

    private fun showDetail(title: String, address: String) {
        placeTitleTextView.text = title
        placeAddressTextView.text = address
        if (placeDetailConstraintLayout.visibility != View.VISIBLE) {
            placeDetailConstraintLayout.visibility = View.VISIBLE
            ObjectAnimator
                    .ofFloat(placeDetailConstraintLayout, "translationY", 0F)
                    .apply {
                        duration = 500
                    }
                    .start()
        }
    }

    private fun hideDetail() {
        ObjectAnimator
                .ofFloat(placeDetailConstraintLayout, "translationY", placeDetailConstraintLayout.height.toFloat())
                .apply {
                    duration = 500
                    addListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            placeDetailConstraintLayout.visibility = View.GONE
                        }
                    })
                }
                .start()
    }
}
