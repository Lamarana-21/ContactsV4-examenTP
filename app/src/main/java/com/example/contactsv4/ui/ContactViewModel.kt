package com.example.contactsv4.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.contactsv4.data.Contact
import com.example.contactsv4.data.ContactDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel partagé pour la gestion des contacts.
 * Effectue toutes les opérations de base de données hors du thread principal (Dispatchers.IO)
 * et expose des LiveData observées par les fragments.
 */
class ContactViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = ContactDao(application)

    private val _contacts = MutableLiveData<List<Contact>>()
    val contacts: LiveData<List<Contact>> get() = _contacts

    private val _selectedContact = MutableLiveData<Contact?>()
    val selectedContact: LiveData<Contact?> get() = _selectedContact

    private var currentSearchQuery: String = ""

    init {
        loadContacts()
    }

    /**
     * Recharge la liste des contacts depuis la base de données.
     */
    fun loadContacts() {
        viewModelScope.launch {
            val list = withContext(Dispatchers.IO) {
                if (currentSearchQuery.isBlank()) {
                    dao.getAll()
                } else {
                    dao.search(currentSearchQuery)
                }
            }
            _contacts.value = list
        }
    }

    /**
     * Effectue une recherche filtrée par texte.
     */
    fun search(query: String) {
        currentSearchQuery = query
        loadContacts()
    }

    /**
     * Charge un contact par son identifiant.
     */
    fun loadContactById(id: Long) {
        if (id <= 0L) {
            _selectedContact.value = null
            return
        }
        viewModelScope.launch {
            val contact = withContext(Dispatchers.IO) {
                dao.getById(id)
            }
            _selectedContact.value = contact
        }
    }

    /**
     * Insère un nouveau contact et rafraîchit la liste.
     */
    fun insertContact(contact: Contact, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val id = withContext(Dispatchers.IO) {
                dao.insert(contact)
            }
            if (id != -1L) {
                loadContacts()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Met à jour un contact existant et rafraîchit la liste.
     */
    fun updateContact(contact: Contact, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) {
                dao.update(contact)
            }
            if (rows > 0) {
                loadContacts()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Supprime un contact par son id et rafraîchit la liste.
     */
    fun deleteContact(id: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val rows = withContext(Dispatchers.IO) {
                dao.delete(id)
            }
            if (rows > 0) {
                loadContacts()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    /**
     * Reinitialise le contact sélectionné.
     */
    fun clearSelectedContact() {
        _selectedContact.value = null
    }
}
