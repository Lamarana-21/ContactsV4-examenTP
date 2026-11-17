package com.example.contactsv4.pref

import android.content.Context
import android.graphics.Color
import androidx.preference.PreferenceManager

/**
 * Centralise la lecture et la conversion des préférences de l'application.
 * Convertit les valeurs lues dans SharedPreferences vers des types exploitables par le code UI (Float sp, Color Int, Boolean).
 */
class PreferencesManager(context: Context) {

    private val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)

    companion object {
        const val KEY_TAILLE_POLICE = "taille_police"
        const val KEY_COULEUR_PREFEREE = "couleur_preferee"
        const val KEY_APPEL_DEPUIS_LISTE = "appel_depuis_liste"

        const val DEFAULT_TAILLE_POLICE = "16"
        const val DEFAULT_COULEUR_PREFEREE = "#121212"
        const val DEFAULT_APPEL_DEPUIS_LISTE = false
    }

    /**
     * Retourne la taille de police en sp sous forme de Float.
     * Valeurs possibles : 14f (Petite), 16f (Moyenne), 20f (Grande).
     */
    fun getTaillePoliceSp(): Float {
        val rawValue = sharedPreferences.getString(KEY_TAILLE_POLICE, DEFAULT_TAILLE_POLICE) ?: DEFAULT_TAILLE_POLICE
        return rawValue.toFloatOrNull() ?: 16f
    }

    /**
     * Retourne la couleur préférée sous forme d'entier d'ARGB (Color.parseColor).
     */
    fun getCouleurPreferee(): Int {
        val rawColor = sharedPreferences.getString(KEY_COULEUR_PREFEREE, DEFAULT_COULEUR_PREFEREE) ?: DEFAULT_COULEUR_PREFEREE
        return try {
            Color.parseColor(rawColor)
        } catch (_: Exception) {
            Color.parseColor(DEFAULT_COULEUR_PREFEREE)
        }
    }

    /**
     * Indique si l'option d'appel direct depuis chaque ligne de la liste est activée.
     */
    fun isAppelDepuisListeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_APPEL_DEPUIS_LISTE, DEFAULT_APPEL_DEPUIS_LISTE)
    }
}
