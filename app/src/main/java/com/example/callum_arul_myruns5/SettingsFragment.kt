package com.example.callum_arul_myruns5

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
class SettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        val webpage: Preference? = findPreference("webpage_preference")
        val link = webpage?.summary?.toString()

        webpage?.setOnPreferenceClickListener { _ ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            startActivity(intent)
            true
        }

        val userProfilePreference: Preference? = findPreference("user_profile")

        userProfilePreference?.setOnPreferenceClickListener { _ ->
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
            true
        }
    }

}