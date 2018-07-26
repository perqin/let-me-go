package com.perqin.letmego.pages.main

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.perqin.letmego.R
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)
        viewModel.selectedPlace.observe(this, Observer {
            if (it != null) {
                showDetail("(${it.latLng.latitude}, ${it.latLng.longitude})")
            } else {
                hideDetail()
            }
        })

        notifyImageButton.setOnClickListener {
            viewModel.toggleEnableNotificationForSelectedPlace()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
        }
    }

    private fun showDetail(title: String) {
        placeTitleTextView.text = title
        placeDetailConstraintLayout.visibility = View.VISIBLE
        ObjectAnimator
                .ofFloat(placeDetailConstraintLayout, "translationY", 0F)
                .apply {
                    duration = 500
                }
                .start()
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
