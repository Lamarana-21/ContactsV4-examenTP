package com.example.contactsv4.ui

import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceFragmentCompat
import com.example.contactsv4.R

/**
 * Fragment gérant l'écran des préférences de l'application.
 * Hérite de PreferenceFragmentCompat pour charger le fichier res/xml/preferences.xml.
 */
class ParametresFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = getString(R.string.title_parametres)
    }
}
