package com.example.contactsv4

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.example.contactsv4.ui.ListeContactsFragment
import com.example.contactsv4.ui.ParametresFragment
import com.google.android.material.appbar.MaterialToolbar

/**
 * Activité principale unique de l'application (Single-Activity Architecture).
 * Gère la Toolbar, le menu de configuration et la pile de fragments (FragmentContainerView).
 */
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Gestion du bouton "Retour" sur la Toolbar selon la pile de fragments
        supportFragmentManager.addOnBackStackChangedListener {
            updateBackButtonVisibility()
        }

        // Chargement du fragment initial uniquement si ce n'est pas un changement d'orientation
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ListeContactsFragment())
                .commit()
        }

        updateBackButtonVisibility()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                openParametresFragment()
                true
            }
            android.R.id.button1, android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            return true
        }
        return super.onSupportNavigateUp()
    }

    /**
     * Ouvre l'écran des paramètres de l'application.
     */
    private fun openParametresFragment() {
        // Évite de réempiler l'écran Paramètres si on y est déjà
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment !is ParametresFragment) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ParametresFragment())
                .addToBackStack(null)
                .commit()
        }
    }

    /**
     * Affiche ou masque la flèche de retour dans l'ActionBar selon qu'un fragment est empilé.
     */
    private fun updateBackButtonVisibility() {
        val showBack = supportFragmentManager.backStackEntryCount > 0
        supportActionBar?.setDisplayHomeAsUpEnabled(showBack)
        supportActionBar?.setDisplayShowHomeEnabled(showBack)
    }
}
