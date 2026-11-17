package com.example.contactsv4.data

import java.io.Serializable

/**
 * Modèle de données représentant un Contact.
 * Implémente Serializable pour permettre le passage d'instances entre fragments si besoin.
 */
data class Contact(
    val id: Long = 0L,
    val nom: String,
    val prenom: String,
    val telephone: String,
    val email: String? = null,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) : Serializable
