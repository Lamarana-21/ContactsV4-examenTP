package com.example.contactsv4.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

/**
 * Unique classe d'accès aux données SQLite (Data Access Object).
 * Toutes les requêtes SQL sont paramétrées et les Cursors sont fermés de manière sécurisée via .use { }.
 */
class ContactDao(context: Context) {

    private val dbHelper = ContactDbHelper.getInstance(context)

    /**
     * Insère un nouveau contact dans la base de données.
     * @return L'identifiant généré (_id) ou -1L en cas d'erreur.
     */
    fun insert(contact: Contact): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.ContactEntry.COLUMN_NOM, contact.nom)
            put(ContactContract.ContactEntry.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.ContactEntry.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.ContactEntry.COLUMN_EMAIL, contact.email)
            put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, contact.photoUri)
            if (contact.latitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, contact.latitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
            }
            if (contact.longitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, contact.longitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            }
        }
        return db.insert(ContactContract.ContactEntry.TABLE_NAME, null, values)
    }

    /**
     * Met à jour un contact existant.
     * @return Nombre de lignes modifiées.
     */
    fun update(contact: Contact): Int {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(ContactContract.ContactEntry.COLUMN_NOM, contact.nom)
            put(ContactContract.ContactEntry.COLUMN_PRENOM, contact.prenom)
            put(ContactContract.ContactEntry.COLUMN_TELEPHONE, contact.telephone)
            put(ContactContract.ContactEntry.COLUMN_EMAIL, contact.email)
            put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, contact.photoUri)
            if (contact.latitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, contact.latitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
            }
            if (contact.longitude != null) {
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, contact.longitude)
            } else {
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            }
        }
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(contact.id.toString())
        return db.update(ContactContract.ContactEntry.TABLE_NAME, values, selection, selectionArgs)
    }

    /**
     * Supprime un contact par son identifiant.
     * @return Nombre de lignes supprimées.
     */
    fun delete(id: Long): Int {
        val db = dbHelper.writableDatabase
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        return db.delete(ContactContract.ContactEntry.TABLE_NAME, selection, selectionArgs)
    }

    /**
     * Récupère tous les contacts, triés par nom puis prénom (insensible à la casse avec COLLATE NOCASE).
     */
    fun getAll(): List<Contact> {
        val db = dbHelper.readableDatabase
        val orderBy = "${ContactContract.ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"

        val contacts = mutableListOf<Contact>()
        db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            orderBy
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    /**
     * Récupère un contact par son identifiant.
     */
    fun getById(id: Long): Contact? {
        val db = dbHelper.readableDatabase
        val selection = "${ContactContract.ContactEntry.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id.toString())

        db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return cursorToContact(cursor)
            }
        }
        return null
    }

    /**
     * Recherche des contacts par texte (insensible à la casse sur nom, prénom et téléphone).
     * Utilise des requêtes SQL paramétrées avec LIKE et la clause COLLATE NOCASE.
     */
    fun search(texte: String): List<Contact> {
        if (texte.isBlank()) {
            return getAll()
        }

        val db = dbHelper.readableDatabase
        val pattern = "%$texte%"
        val selection = "(${ContactContract.ContactEntry.COLUMN_NOM} LIKE ? COLLATE NOCASE " +
                "OR ${ContactContract.ContactEntry.COLUMN_PRENOM} LIKE ? COLLATE NOCASE " +
                "OR ${ContactContract.ContactEntry.COLUMN_TELEPHONE} LIKE ? COLLATE NOCASE)"
        val selectionArgs = arrayOf(pattern, pattern, pattern)
        val orderBy = "${ContactContract.ContactEntry.COLUMN_NOM} COLLATE NOCASE ASC, ${ContactContract.ContactEntry.COLUMN_PRENOM} COLLATE NOCASE ASC"

        val contacts = mutableListOf<Contact>()
        db.query(
            ContactContract.ContactEntry.TABLE_NAME,
            null,
            selection,
            selectionArgs,
            null,
            null,
            orderBy
        ).use { cursor ->
            while (cursor.moveToNext()) {
                contacts.add(cursorToContact(cursor))
            }
        }
        return contacts
    }

    /**
     * Méthode utilitaire de conversion d'une ligne de Cursor en objet Contact.
     * Utilise getColumnIndexOrThrow pour la robustesse.
     */
    private fun cursorToContact(cursor: Cursor): Contact {
        val id = cursor.getLong(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_ID))
        val nom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_NOM))
        val prenom = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_PRENOM))
        val telephone = cursor.getString(cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_TELEPHONE))

        val emailIdx = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_EMAIL)
        val email = if (cursor.isNull(emailIdx)) null else cursor.getString(emailIdx)

        val photoIdx = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_PHOTO_URI)
        val photoUri = if (cursor.isNull(photoIdx)) null else cursor.getString(photoIdx)

        val latIdx = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_LATITUDE)
        val latitude = if (cursor.isNull(latIdx)) null else cursor.getDouble(latIdx)

        val lonIdx = cursor.getColumnIndexOrThrow(ContactContract.ContactEntry.COLUMN_LONGITUDE)
        val longitude = if (cursor.isNull(lonIdx)) null else cursor.getDouble(lonIdx)

        return Contact(
            id = id,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude
        )
    }
}
