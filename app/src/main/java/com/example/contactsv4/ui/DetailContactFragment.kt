package com.example.contactsv4.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.contactsv4.R
import com.example.contactsv4.data.Contact
import com.example.contactsv4.pref.PreferencesManager
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Fragment pour la création, consultation, modification et suppression d'un contact.
 * Gère la validation des champs, la sauvegarde sécurisée de la photo dans filesDir, et les intents d'email/carte.
 */
class DetailContactFragment : Fragment() {

    companion object {
        private const val ARG_CONTACT_ID = "contact_id"

        fun newInstance(contactId: Long): DetailContactFragment {
            val fragment = DetailContactFragment()
            val args = Bundle().apply {
                putLong(ARG_CONTACT_ID, contactId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var preferencesManager: PreferencesManager

    private var contactId: Long = -1L
    private var currentPhotoPath: String? = null
    private var isCreationMode: Boolean = true

    private lateinit var ivPhoto: ImageView
    private lateinit var btnPickGallery: Button
    private lateinit var btnTakePhoto: Button

    private lateinit var tilNom: TextInputLayout
    private lateinit var etNom: EditText
    private lateinit var tilPrenom: TextInputLayout
    private lateinit var etPrenom: EditText
    private lateinit var tilTelephone: TextInputLayout
    private lateinit var etTelephone: EditText
    private lateinit var tilEmail: TextInputLayout
    private lateinit var etEmail: EditText
    private lateinit var btnSendEmail: ImageButton
    private lateinit var tilLatitude: TextInputLayout
    private lateinit var etLatitude: EditText
    private lateinit var tilLongitude: TextInputLayout
    private lateinit var etLongitude: EditText

    private lateinit var btnViewMap: Button
    private lateinit var btnCancel: Button
    private lateinit var btnSave: Button
    private lateinit var btnDelete: Button

    // Contract pour choisir une photo dans la galerie
    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { saveImageFromUriToInternalStorage(it) }
    }

    // Contract pour prendre une photo avec l'appareil photo
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let { saveBitmapToInternalStorage(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getLong(ARG_CONTACT_ID, -1L) ?: -1L
        isCreationMode = (contactId <= 0L)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferencesManager = PreferencesManager(requireContext())
        initViews(view)
        applyPreferences()
        setupListeners()

        if (!isCreationMode) {
            viewModel.loadContactById(contactId)
            observeContact()
        } else {
            activity?.title = getString(R.string.title_nouveau_contact)
            btnDelete.visibility = View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Réinitialise la sélection dans le ViewModel lors de la fermeture du fragment
        viewModel.clearSelectedContact()
    }

    private fun initViews(view: View) {
        ivPhoto = view.findViewById(R.id.iv_detail_photo)
        btnPickGallery = view.findViewById(R.id.btn_pick_gallery)
        btnTakePhoto = view.findViewById(R.id.btn_take_photo)

        tilNom = view.findViewById(R.id.til_nom)
        etNom = view.findViewById(R.id.et_nom)
        tilPrenom = view.findViewById(R.id.til_prenom)
        etPrenom = view.findViewById(R.id.et_prenom)
        tilTelephone = view.findViewById(R.id.til_telephone)
        etTelephone = view.findViewById(R.id.et_telephone)
        tilEmail = view.findViewById(R.id.til_email)
        etEmail = view.findViewById(R.id.et_email)
        btnSendEmail = view.findViewById(R.id.btn_send_email)
        tilLatitude = view.findViewById(R.id.til_latitude)
        etLatitude = view.findViewById(R.id.et_latitude)
        tilLongitude = view.findViewById(R.id.til_longitude)
        etLongitude = view.findViewById(R.id.et_longitude)

        btnViewMap = view.findViewById(R.id.btn_view_map)
        btnCancel = view.findViewById(R.id.btn_cancel)
        btnSave = view.findViewById(R.id.btn_save)
        btnDelete = view.findViewById(R.id.btn_delete)
    }

    /**
     * Applique les préférences utilisateur (taille de police et couleur) aux champs du formulaire.
     */
    private fun applyPreferences() {
        val fontSize = preferencesManager.getTaillePoliceSp()
        val color = preferencesManager.getCouleurPreferee()

        etNom.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        etPrenom.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)
        etTelephone.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize)

        etNom.setTextColor(color)
        etPrenom.setTextColor(color)
    }

    private fun setupListeners() {
        btnPickGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnTakePhoto.setOnClickListener {
            cameraLauncher.launch(null)
        }

        btnSendEmail.setOnClickListener {
            sendEmail()
        }

        btnViewMap.setOnClickListener {
            openMapFragment()
        }

        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSave.setOnClickListener {
            saveContact()
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }
    }

    private fun observeContact() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { contact ->
            contact?.let {
                activity?.title = getString(R.string.title_detail_contact)
                etNom.setText(it.nom)
                etPrenom.setText(it.prenom)
                etTelephone.setText(it.telephone)
                etEmail.setText(it.email ?: "")
                etLatitude.setText(it.latitude?.toString() ?: "")
                etLongitude.setText(it.longitude?.toString() ?: "")

                currentPhotoPath = it.photoUri
                displayPhoto(currentPhotoPath)
                btnDelete.visibility = View.VISIBLE
            }
        }
    }

    private fun displayPhoto(path: String?) {
        if (!path.isNullOrEmpty()) {
            val imgFile = File(path)
            if (imgFile.exists()) {
                val bitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                ivPhoto.setImageBitmap(bitmap)
            } else {
                try {
                    ivPhoto.setImageURI(Uri.parse(path))
                } catch (_: Exception) {
                    ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
                }
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_person_placeholder)
        }
    }

    /**
     * Copie l'image sélectionnée depuis la galerie dans les fichiers internes (filesDir)
     * et enregistre le chemin local résultant.
     */
    private fun saveImageFromUriToInternalStorage(uri: Uri) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)

            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }

            currentPhotoPath = file.absolutePath
            displayPhoto(currentPhotoPath)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'enregistrement de l'image.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Copie le bitmap capturé depuis la caméra dans les fichiers internes (filesDir).
     */
    private fun saveBitmapToInternalStorage(bitmap: Bitmap) {
        try {
            val fileName = "photo_${System.currentTimeMillis()}.jpg"
            val file = File(requireContext().filesDir, fileName)

            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)
            }

            currentPhotoPath = file.absolutePath
            displayPhoto(currentPhotoPath)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), "Erreur lors de l'enregistrement de la photo.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Validation et enregistrement du contact.
     */
    private fun saveContact() {
        val nom = etNom.text.toString().trim()
        val prenom = etPrenom.text.toString().trim()
        val telephone = etTelephone.text.toString().trim()
        val email = etEmail.text.toString().trim().ifBlank { null }

        // Validation des champs obligatoires
        if (nom.isEmpty() || prenom.isEmpty() || telephone.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.error_required_fields), Toast.LENGTH_LONG).show()
            if (nom.isEmpty()) tilNom.error = "Obligatoire" else tilNom.error = null
            if (prenom.isEmpty()) tilPrenom.error = "Obligatoire" else tilPrenom.error = null
            if (telephone.isEmpty()) tilTelephone.error = "Obligatoire" else tilTelephone.error = null
            return
        }

        // Effacer les erreurs
        tilNom.error = null
        tilPrenom.error = null
        tilTelephone.error = null

        // Lecture et validation facultative des coordonnées GPS
        val latStr = etLatitude.text.toString().trim()
        val lonStr = etLongitude.text.toString().trim()

        val latitude: Double? = if (latStr.isNotEmpty()) latStr.toDoubleOrNull() else null
        val longitude: Double? = if (lonStr.isNotEmpty()) lonStr.toDoubleOrNull() else null

        if ((latStr.isNotEmpty() && latitude == null) || (lonStr.isNotEmpty() && longitude == null)) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_coordinates), Toast.LENGTH_SHORT).show()
            return
        }

        if (latitude != null && (latitude < -90.0 || latitude > 90.0)) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_coordinates), Toast.LENGTH_SHORT).show()
            return
        }

        if (longitude != null && (longitude < -180.0 || longitude > 180.0)) {
            Toast.makeText(requireContext(), getString(R.string.error_invalid_coordinates), Toast.LENGTH_SHORT).show()
            return
        }

        val contact = Contact(
            id = if (isCreationMode) 0L else contactId,
            nom = nom,
            prenom = prenom,
            telephone = telephone,
            email = email,
            photoUri = currentPhotoPath,
            latitude = latitude,
            longitude = longitude
        )

        if (isCreationMode) {
            viewModel.insertContact(contact) { success ->
                if (success) {
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la création du contact.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            viewModel.updateContact(contact) { success ->
                if (success) {
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Erreur lors de la modification du contact.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * Ouvre le fragment de géolocalisation Google Maps en lui passant l'ID du contact (_id).
     */
    private fun openMapFragment() {
        val carteFragment = CarteContactFragment.newInstance(contactId)

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, carteFragment)
            .addToBackStack(null)
            .commit()
    }

    /**
     * Envoie un e-mail au contact via ACTION_SENDTO.
     */
    private fun sendEmail() {
        val mail = etEmail.text.toString().trim()
        if (mail.isEmpty()) {
            Toast.makeText(requireContext(), "Veuillez saisir une adresse e-mail.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$mail")
        }

        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(requireContext(), getString(R.string.no_email_app), Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Boîte de dialogue de confirmation avant suppression.
     */
    private fun confirmDelete() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirm_title))
            .setMessage(getString(R.string.delete_confirm_message))
            .setPositiveButton(getString(R.string.confirm_yes)) { _, _ ->
                viewModel.deleteContact(contactId) { success ->
                    if (success) {
                        parentFragmentManager.popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Erreur lors de la suppression.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.confirm_no), null)
            .show()
    }
}
