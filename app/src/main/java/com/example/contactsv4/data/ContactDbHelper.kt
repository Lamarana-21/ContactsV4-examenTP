package com.example.contactsv4.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Helper SQLite créant et pré-remplissant la base de données contacts.db.
 * Implémenté en Singleton afin d'éviter les fuites de mémoire et les accès concurrents non sécurisés.
 */
class ContactDbHelper private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    ContactContract.DATABASE_NAME,
    null,
    ContactContract.DATABASE_VERSION
) {

    companion object {
        @Volatile
        private var instance: ContactDbHelper? = null

        fun getInstance(context: Context): ContactDbHelper {
            return instance ?: synchronized(this) {
                instance ?: ContactDbHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(ContactContract.SQL_CREATE_ENTRIES)
        seedDemoData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(ContactContract.SQL_DELETE_ENTRIES)
        onCreate(db)
    }

    /**
     * Insertion de 5 contacts de démonstration (dont 2 sans coordonnées GPS).
     */
    private fun seedDemoData(db: SQLiteDatabase) {
        val contacts = listOf(
            ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, "Ben Ali")
                put(ContactContract.ContactEntry.COLUMN_PRENOM, "Mohamed")
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, "+216 98 123 456")
                put(ContactContract.ContactEntry.COLUMN_EMAIL, "mohamed.benali@email.tn")
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, null as String?)
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, 36.8065) // Tunis
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, 10.1815)
            },
            ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, "Trabelsi")
                put(ContactContract.ContactEntry.COLUMN_PRENOM, "Sarra")
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, "+216 22 987 654")
                put(ContactContract.ContactEntry.COLUMN_EMAIL, "sarra.trabelsi@email.tn")
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, null as String?)
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, 35.8256) // Sousse
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, 10.6369)
            },
            ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, "Gharbi")
                put(ContactContract.ContactEntry.COLUMN_PRENOM, "Ahmed")
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, "+216 55 444 333")
                put(ContactContract.ContactEntry.COLUMN_EMAIL, "ahmed.gharbi@email.tn")
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, null as String?)
                put(ContactContract.ContactEntry.COLUMN_LATITUDE, 34.7406) // Sfax
                put(ContactContract.ContactEntry.COLUMN_LONGITUDE, 10.7603)
            },
            // Contact de démonstration 1 sans GPS
            ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, "Dridi")
                put(ContactContract.ContactEntry.COLUMN_PRENOM, "Yassine")
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, "+216 20 111 222")
                put(ContactContract.ContactEntry.COLUMN_EMAIL, "yassine.dridi@email.tn")
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, null as String?)
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            },
            // Contact de démonstration 2 sans GPS
            ContentValues().apply {
                put(ContactContract.ContactEntry.COLUMN_NOM, "Ouni")
                put(ContactContract.ContactEntry.COLUMN_PRENOM, "Meriem")
                put(ContactContract.ContactEntry.COLUMN_TELEPHONE, "+216 99 888 777")
                put(ContactContract.ContactEntry.COLUMN_EMAIL, "meriem.ouni@email.tn")
                put(ContactContract.ContactEntry.COLUMN_PHOTO_URI, null as String?)
                putNull(ContactContract.ContactEntry.COLUMN_LATITUDE)
                putNull(ContactContract.ContactEntry.COLUMN_LONGITUDE)
            }
        )

        for (cv in contacts) {
            db.insert(ContactContract.ContactEntry.TABLE_NAME, null, cv)
        }
    }
}
