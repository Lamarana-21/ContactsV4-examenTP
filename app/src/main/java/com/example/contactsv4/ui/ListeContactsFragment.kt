package com.example.contactsv4.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv4.R
import com.example.contactsv4.pref.PreferencesManager
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Fragment affichant la liste des contacts avec filtrage temps réel et gestion des préférences.
 */
class ListeContactsFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var adapter: ContactAdapter

    private lateinit var etSearch: EditText
    private lateinit var rvContacts: RecyclerView
    private lateinit var tvEmpty: TextView
    private lateinit var fabAdd: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liste_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())

        etSearch = view.findViewById(R.id.et_search)
        rvContacts = view.findViewById(R.id.rv_contacts)
        tvEmpty = view.findViewById(R.id.tv_empty)
        fabAdd = view.findViewById(R.id.fab_add)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Titre de l'ActionBar
        activity?.title = getString(R.string.title_liste_contacts)

        // Application immédiate des préférences utilisateur sans redémarrer
        val fontSize = preferencesManager.getTaillePoliceSp()
        val color = preferencesManager.getCouleurPreferee()
        val showCall = preferencesManager.isAppelDepuisListeEnabled()
        adapter.updatePreferences(fontSize, color, showCall)

        // Rechargement des données
        viewModel.loadContacts()
    }

    private fun setupRecyclerView() {
        adapter = ContactAdapter(
            onContactClick = { contact ->
                openDetailFragment(contact.id)
            },
            onCallClick = { contact ->
                makePhoneCall(contact.telephone)
            }
        )
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter
    }

    private fun setupListeners() {
        // Filtrage dynamique au fil de la saisie
        etSearch.doAfterTextChanged { text ->
            viewModel.search(text?.toString() ?: "")
        }

        // Bouton FAB d'ajout de contact
        fabAdd.setOnClickListener {
            openDetailFragment(-1L)
        }
    }

    private fun observeViewModel() {
        viewModel.contacts.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            if (list.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvContacts.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                rvContacts.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Ouvre le fragment détail en lui passant l'identifiant du contact (_id).
     */
    private fun openDetailFragment(contactId: Long) {
        val detailFragment = DetailContactFragment.newInstance(contactId)
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Lance l'action d'appel via ACTION_DIAL (aucune permission spécifique requise).
     */
    private fun makePhoneCall(phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(requireContext(), getString(R.string.no_phone_app), Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:${phone.trim()}")
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), getString(R.string.no_phone_app), Toast.LENGTH_SHORT).show()
        }
    }
}
