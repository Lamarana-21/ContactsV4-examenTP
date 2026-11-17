package com.example.contactsv4.data

/**
 * Contrat définissant les constantes de la base de données SQLite pour les contacts.
 * Centralise le nom de la table, des colonnes et la requête de création.
 */
object ContactContract {
    const val DATABASE_NAME = "contacts.db"
    const val DATABASE_VERSION = 1

    object ContactEntry {
        const val TABLE_NAME = "contacts"
        const val COLUMN_ID = "_id"
        const val COLUMN_NOM = "nom"
        const val COLUMN_PRENOM = "prenom"
        const val COLUMN_TELEPHONE = "telephone"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_PHOTO_URI = "photo_uri"
        const val COLUMN_LATITUDE = "latitude"
        const val COLUMN_LONGITUDE = "longitude"
    }

    const val SQL_CREATE_ENTRIES =
        "CREATE TABLE ${ContactEntry.TABLE_NAME} (" +
                "${ContactEntry.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${ContactEntry.COLUMN_NOM} TEXT NOT NULL, " +
                "${ContactEntry.COLUMN_PRENOM} TEXT NOT NULL, " +
                "${ContactEntry.COLUMN_TELEPHONE} TEXT NOT NULL, " +
                "${ContactEntry.COLUMN_EMAIL} TEXT, " +
                "${ContactEntry.COLUMN_PHOTO_URI} TEXT, " +
                "${ContactEntry.COLUMN_LATITUDE} REAL, " +
                "${ContactEntry.COLUMN_LONGITUDE} REAL" +
                ");"

    const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${ContactEntry.TABLE_NAME}"
}
