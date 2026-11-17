package com.example.contactsv4.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.example.contactsv4.R
import com.example.contactsv4.data.Contact
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Fragment pour l'affichage de la position du contact sur OpenStreetMap (OSMDroid).
 * OpenStreetMap est 100% gratuit, open-source et ne nécessite aucune clé API Google !
 */
class CarteContactFragment : Fragment() {

    companion object {
        const val ARG_CONTACT_ID = "contact_id"

        /**
         * Crée une nouvelle instance du fragment en lui passant l'ID du contact.
         */
        fun newInstance(id: Long): CarteContactFragment {
            val fragment = CarteContactFragment()
            val args = Bundle().apply {
                putLong(ARG_CONTACT_ID, id)
            }
            fragment.arguments = args
            return fragment
        }
    }

    private val viewModel: ContactViewModel by activityViewModels()

    private var contactId: Long = -1L
    private var contact: Contact? = null

    private lateinit var tvMapError: TextView
    private lateinit var mapView: MapView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contactId = arguments?.getLong(ARG_CONTACT_ID, -1L) ?: -1L

        // Configuration d'OSMDroid pour charger les cartes OpenStreetMap
        val ctx = requireContext().applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = ctx.packageName
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_carte_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.title_carte_contact)

        tvMapError = view.findViewById(R.id.tv_map_error)
        mapView = view.findViewById(R.id.map_view)

        // Configuration initiale de la vue OpenStreetMap
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        if (contactId > 0L) {
            viewModel.loadContactById(contactId)
            observeContact()
        } else {
            showError("Contact introuvable.")
        }
    }

    private fun observeContact() {
        viewModel.selectedContact.observe(viewLifecycleOwner) { loadedContact ->
            if (loadedContact == null) {
                showError("Contact introuvable.")
                return@observe
            }

            this.contact = loadedContact
            val lat = loadedContact.latitude
            val lon = loadedContact.longitude

            if (isValidGps(lat, lon)) {
                tvMapError.visibility = View.GONE
                displayMap(loadedContact, lat!!, lon!!)
            } else {
                showError("Aucune position valide pour ce contact.")
            }
        }
    }

    private fun displayMap(contact: Contact, lat: Double, lon: Double) {
        val startPoint = GeoPoint(lat, lon)

        // Placement du centre de la carte et du zoom (15)
        mapView.controller.setZoom(15.0)
        mapView.controller.setCenter(startPoint)

        // Placement du marqueur titré "Prénom Nom"
        val markerTitle = "${contact.prenom} ${contact.nom}".trim()
        val marker = Marker(mapView)
        marker.position = startPoint
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = if (markerTitle.isNotBlank()) markerTitle else "Contact"

        mapView.overlays.clear()
        mapView.overlays.add(marker)
        mapView.invalidate()
    }

    private fun showError(message: String) {
        tvMapError.visibility = View.VISIBLE
        tvMapError.text = message
    }

    private fun isValidGps(lat: Double?, lon: Double?): Boolean {
        if (lat == null || lon == null) return false
        return (lat >= -90.0 && lat <= 90.0) && (lon >= -180.0 && lon <= 180.0)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}
