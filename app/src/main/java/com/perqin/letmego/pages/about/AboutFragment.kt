package com.perqin.letmego.pages.about

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.perqin.letmego.BuildConfig
import com.perqin.letmego.R

class AboutFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_about, null)
        findPreference<Preference>("about_version")!!.summary = BuildConfig.VERSION_NAME
    }
}
