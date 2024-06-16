package com.example.tfg_manuel_prieto

import Torneo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class TorneosParticipadosAdapter(
    private val torneosList: List<Torneo>,
    private val onPartidosClicked: (Torneo) -> Unit
) : RecyclerView.Adapter<TorneosParticipadosAdapter.TorneoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_torneo_participado, parent, false)
        return TorneoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
        val torneo = torneosList[position]
        holder.bind(torneo)
    }

    override fun getItemCount(): Int = torneosList.size

    inner class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), OnMapReadyCallback {

        private lateinit var googleMap: GoogleMap
        private val mapa: MapView = itemView.findViewById(R.id.mapFragment)
        private val nombreTorneo: TextView = itemView.findViewById(R.id.nombreTorneo)
        private val localidad: TextView = itemView.findViewById(R.id.localidad)
        private val deporte: TextView = itemView.findViewById(R.id.deporte)
        private val fechaInicioIns: TextView = itemView.findViewById(R.id.fechaInicioIns)
        private val fechaFinIns: TextView = itemView.findViewById(R.id.fechaFinIns)
        private val fechaInicio: TextView = itemView.findViewById(R.id.fechaInicio)
        private val fechaFin: TextView = itemView.findViewById(R.id.fechaFin)
        private val btnPartidos: Button = itemView.findViewById(R.id.btnPartidos)

        init {
            mapa.onCreate(null)
            mapa.getMapAsync(this)
            mapa.onResume()
        }

        fun bind(torneo: Torneo) {
            nombreTorneo.text = torneo.nombre
            localidad.text = "Localidad: ${torneo.localidad}"
            deporte.text = "Deporte: ${torneo.deporte}"
            fechaInicioIns.text = "Inicio Inscripción: ${torneo.fechaComienzoInscripcion}"
            fechaFinIns.text = "Fin Inscripción: ${torneo.fechaFinInscripcion}"
            fechaInicio.text = "Fecha de Inicio: ${torneo.fechaComienzo}"
            fechaFin.text = "Fecha de Fin: ${torneo.fechaFin}"

            btnPartidos.setOnClickListener {
                onPartidosClicked(torneo)
            }
        }

        override fun onMapReady(map: GoogleMap) {
            googleMap = map
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val torneo = torneosList[position]
                val lat = torneo.latitud?.toDoubleOrNull()
                val lng = torneo.longitud?.toDoubleOrNull()
                if (lat != null && lng != null) {
                    val location = LatLng(lat, lng)
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(location).title(torneo.nombre))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
                }
            }
        }
    }
}