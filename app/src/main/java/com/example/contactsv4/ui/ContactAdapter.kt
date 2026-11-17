package com.example.contactsv4.ui

import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.contactsv4.R
import com.example.contactsv4.data.Contact
import java.io.File

/**
 * Adapter RecyclerView pour afficher la liste des contacts.
 * Gère l'application dynamique des préférences utilisateur (taille de police, couleur, bouton d'appel).
 */
class ContactAdapter(
    private val onContactClick: (Contact) -> Unit,
    private val onCallClick: (Contact) -> Unit
) : ListAdapter<Contact, ContactAdapter.ContactViewHolder>(ContactDiffCallback()) {

    private var fontSizeSp: Float = 16f
    private var nameColor: Int = Color.BLACK
    private var showCallButton: Boolean = false

    fun updatePreferences(fontSizeSp: Float, nameColor: Int, showCallButton: Boolean) {
        this.fontSizeSp = fontSizeSp
        this.nameColor = nameColor
        this.showCallButton = showCallButton
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = getItem(position)
        holder.bind(contact, fontSizeSp, nameColor, showCallButton, onContactClick, onCallClick)
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivPhoto: ImageView = itemView.findViewById(R.id.iv_photo)
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_phone)
        private val btnCall: ImageButton = itemView.findViewById(R.id.btn_call)

        fun bind(
            contact: Contact,
            fontSizeSp: Float,
            nameColor: Int,
            showCallButton: Boolean,
            onContactClick: (Contact) -> Unit,
            onCallClick: (Contact) -> Unit
        ) {
            val fullName = "${contact.nom} ${contact.prenom}"
            tvName.text = fullName
            tvPhone.text = contact.telephone

            // Application des préférences dynamiques
            tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp)
            tvPhone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp * 0.9f)
            tvName.setTextColor(nameColor)

            // Gestion du bouton d'appel direct
            if (showCallButton) {
                btnCall.visibility = View.VISIBLE
                btnCall.setOnClickListener { onCallClick(contact) }
            } else {
                btnCall.visibility = View.GONE
                btnCall.setOnClickListener(null)
            }

            // Chargement de la photo du contact
            val photoUriStr = contact.photoUri
            if (!photoUriStr.isNullOrEmpty()) {
                val imgFile = File(photoUriStr)
                if (imgFile.exists()) {
                    val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                    ivPhoto.setImageBitmap(bitmap)
                } else {
                    try {
                        ivPhoto.setImageURI(Uri.parse(photoUriStr))
                    } catch (_: Exception) {
                        ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
                    }
                }
            } else {
                ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
            }

            // Clic sur toute la ligne -> Ouverture du détail
            itemView.setOnClickListener { onContactClick(contact) }
        }
    }

    private class ContactDiffCallback : DiffUtil.ItemCallback<Contact>() {
        override fun areItemsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Contact, newItem: Contact): Boolean {
            return oldItem == newItem
        }
    }
}
