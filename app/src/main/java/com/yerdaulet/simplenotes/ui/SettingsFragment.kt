package com.yerdaulet.simplenotes.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.yerdaulet.simplenotes.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}