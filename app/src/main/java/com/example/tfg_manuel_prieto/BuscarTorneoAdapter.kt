package com.example.tfg_manuel_prieto

import Torneo
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions



class BuscarTorneoAdapter(private var torneosList: List<Torneo>) :
    RecyclerView.Adapter<BuscarTorneoAdapter.TorneoViewHolder>() {

    private val marcadores = mutableListOf<Marker>()
    private var isCameraAnimating = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_torneo_buscado, parent, false)
        return TorneoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
        val torneo = torneosList[position]
        holder.bind(torneo)
    }

    override fun getItemCount(): Int {
        return torneosList.size
    }

    fun updateData(newTorneosList: List<Torneo>) {
        isCameraAnimating = false // Detener la animación de la cámara si está en curso
        torneosList = newTorneosList
        limpiarMarcadores() // Limpiar todos los marcadores
        notifyDataSetChanged()
    }

    private fun limpiarMarcadores() {
        marcadores.forEach { it.remove() } // Eliminar todos los marcadores del mapa
        marcadores.clear() // Limpiar el registro de marcadores
    }

    inner class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        OnMapReadyCallback {

        private lateinit var googleMap: GoogleMap
        private lateinit var mapa: MapView
        private var torneo: Torneo? = null

        private val nombreTorneo: TextView = itemView.findViewById(R.id.nombreTorneo)
        private val localidad: TextView = itemView.findViewById(R.id.localidad)
        private val deporte: TextView = itemView.findViewById(R.id.deporte)
        private val fechaInicioIns: TextView = itemView.findViewById(R.id.fechaInicioIns)
        private val fechaFinIns: TextView = itemView.findViewById(R.id.fechaFinIns)
        private val fechaInicio: TextView = itemView.findViewById(R.id.fechaInicio)
        private val fechaFin: TextView = itemView.findViewById(R.id.fechaFin)
        private val btnInscribirse: Button = itemView.findViewById(R.id.btnInscribirse)

        init {
            mapa = itemView.findViewById(R.id.mapFragment)
            mapa.onCreate(null) // Necesario para que el mapa funcione correctamente
            mapa.getMapAsync(this)
            mapa.onResume() // Necesario para que el mapa funcione correctamente
        }

        fun bind(torneo: Torneo) {
            this.torneo = torneo
            nombreTorneo.text = torneo.nombre
            localidad.text = "Localidad: ${torneo.localidad}"
            deporte.text = "Deporte: ${torneo.deporte}"
            fechaInicioIns.text = "Inicio Inscripción: ${torneo.fechaComienzoInscripcion}"
            fechaFinIns.text = "Fin Inscripción: ${torneo.fechaFinInscripcion}"
            fechaInicio.text = "Fecha de Inicio: ${torneo.fechaComienzo}"
            fechaFin.text = "Fecha de Fin: ${torneo.fechaFin}"

            btnInscribirse.setOnClickListener {
                val intent = Intent(itemView.context, CrearEquipoActivity::class.java).apply {
                    putExtra("TORNEO_ID", torneo.id)
                    putExtra("TORNEO_NOMBRE", torneo.nombre)
                    putExtra("MAX_PARTICIPANTES", torneo.participantes)
                }
                itemView.context.startActivity(intent)
            }

            // Refrescar el mapa con la nueva ubicación del torneo
            if (::googleMap.isInitialized) {
                actualizarMapa(torneo)
            }
        }

        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            val torneo = this.torneo ?: return // Salir si el torneo aún no se ha establecido
            actualizarMapa(torneo)
        }

        private fun actualizarMapa(torneo: Torneo) {
            val lat = torneo.latitud?.toDoubleOrNull()
            val lng = torneo.longitud?.toDoubleOrNull()
            if (lat != null && lng != null) {
                val location = LatLng(lat, lng)
                googleMap.clear() // Limpiar el mapa antes de agregar un nuevo marcador
                val marker = googleMap.addMarker(MarkerOptions().position(location).title(torneo.nombre))
                marker?.let { marcadores.add(it) } // Agregar el marcador al registro
                if (!isCameraAnimating) {
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                }
            }
        }
    }
}