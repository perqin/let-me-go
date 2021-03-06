package com.perqin.letmego.pages.main

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.HtmlCompat
import com.perqin.letmego.R
import com.perqin.letmego.data.destination.Destination
import com.perqin.letmego.data.place.Place
import com.perqin.letmego.data.preferences.PreferencesRepo
import com.perqin.letmego.pages.about.AboutActivity
import com.perqin.letmego.pages.destinationlist.DestinationListFragment
import com.perqin.letmego.pages.main.map.MapFragment
import com.perqin.letmego.pages.main.permissions.PermissionsFragment
import com.perqin.letmego.pages.main.search.SearchDestinationFragment
import com.perqin.letmego.utils.permissionsAllGranted
import com.perqin.letmego.utils.privacyPolicyUrl

class MainActivity : AppCompatActivity(), PermissionsFragment.Callback, MapFragment.Callback,
        DestinationListFragment.Callback, SearchDestinationFragment.Callback {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!PreferencesRepo.privacyPolicyAccepted) {
            showPrivacyPolicy(true)
            return
        }

        startApp()

        handleSearchIfNeeded(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleSearchIfNeeded(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.action_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.privacyPolicyItem -> {
                showPrivacyPolicy(false)
                true
            }
            R.id.aboutItem -> {
                startActivity(Intent(this, AboutActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAllPermissionsGranted() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainerView, MapFragment.newInstance(), FRAGMENT_MAP)
                .commit()
    }

    override fun openDestinationSearch() {
        supportFragmentManager.beginTransaction()
                .add(R.id.fragmentContainerView, SearchDestinationFragment.newInstance())
                .addToBackStack(null)
                .commit()
    }

    override fun locatePlaceAndClose(place: Place) {
        (supportFragmentManager.findFragmentByTag(FRAGMENT_MAP) as MapFragment).selectPlace(place)
        closeSearch()
    }

    override fun closeSearch() {
        supportFragmentManager.popBackStack()
    }

    override fun onSelectDestination(destination: Destination) {
        (supportFragmentManager.findFragmentByTag(FRAGMENT_MAP) as? MapFragment)?.selectDestination(destination)
    }

    private fun handleSearchIfNeeded(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEARCH) {
            val query = intent.getStringExtra(SearchManager.QUERY)?:return
            (supportFragmentManager.findFragmentByTag(FRAGMENT_MAP) as? MapFragment)?.searchDestination(query)
        }
    }

    private fun showPrivacyPolicy(requireAcceptance: Boolean) {
        if (requireAcceptance) {
            val message = LayoutInflater.from(this).inflate(R.layout.dialog_privacy_policy, null) as TextView
            message.text = HtmlCompat.fromHtml(getString(R.string.richText_privacyPolicyMessage, privacyPolicyUrl), 0)
            message.movementMethod = LinkMovementMethod.getInstance()
            AlertDialog.Builder(this)
                    .setTitle(R.string.label_privacyPolicy)
                    .setView(message)
                    .setPositiveButton(R.string.label_accept) { _, _ ->
                        PreferencesRepo.privacyPolicyAccepted = true
                        startApp()
                    }
                    .setNegativeButton(R.string.label_exit) { _, _ ->
                        finish()
                    }
                    .setCancelable(false)
                    .show()
        } else {
            startActivity(Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(privacyPolicyUrl)
            })
        }
    }

    private fun startApp() {
        supportFragmentManager.beginTransaction().apply {
            if (!permissionsAllGranted()) {
                add(R.id.fragmentContainerView, PermissionsFragment.newInstance())
            } else {
                add(R.id.fragmentContainerView, MapFragment.newInstance(), FRAGMENT_MAP)
            }
        }.commit()
    }

    companion object {
        private const val FRAGMENT_MAP = "map"
    }
}
