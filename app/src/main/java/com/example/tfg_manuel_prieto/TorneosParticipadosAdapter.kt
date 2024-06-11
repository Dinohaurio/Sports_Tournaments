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

    private val marcadores = mutableListOf<Marker>()
    private var isCameraAnimating = false

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
        private val btnPartidos: Button = itemView.findViewById(R.id.btnPartidos)

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

            btnPartidos.setOnClickListener {
                onPartidosClicked(torneo)
            }

            generarYMostrarPartidos(torneo)
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

        private fun generarYMostrarPartidos(torneo: Torneo) {
            val fechaFinInscripcion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(torneo.fechaFinInscripcion)
            val fechaHoy = Calendar.getInstance().time
            if (fechaHoy > fechaFinInscripcion) {
                GlobalScope.launch(Dispatchers.Main) {
                    obtenerEquiposInscritosAlTorneo(torneo) { equipos ->
                        val partidos = generarPartidos(equipos, torneo)
                        guardarPartidosEnBaseDeDatos(partidos, torneo.id!!)
                        // Aquí podrías mostrar los partidos en tu interfaz de usuario, si es necesario
                    }
                }
            }
        }

        private fun obtenerEquiposInscritosAlTorneo(torneo: Torneo, callback: (List<Equipo>) -> Unit) {
            val equipos: MutableList<Equipo> = mutableListOf()
            val database = FirebaseDatabase.getInstance().reference
            val query: Query = database.child("equipos").orderByChild("idTorneo").equalTo(torneo.id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (equipoSnapshot in dataSnapshot.children) {
                        val equipo = equipoSnapshot.getValue(Equipo::class.java)
                        equipo?.let { equipos.add(it) }
                    }
                    // Llamar al callback con la lista de equipos obtenidos
                    callback(equipos)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si la consulta se cancela
                    // Aquí puedes mostrar un mensaje de error o registrar el error en los registros
                }
            })
        }

        private fun generarPartidos(equipos: List<Equipo>, torneo: Torneo): List<Partido> {
            val partidos = mutableListOf<Partido>()
            for (i in 0 until equipos.size - 1) {
                for (j in i + 1 until equipos.size) {
                    val equipo1 = equipos[i]
                    val equipo2 = equipos[j]
                    val partido = Partido(
                        id = UUID.randomUUID().toString(),
                        equipo1 = equipo1.toString(),
                        equipo2 = equipo2.toString(),
                        marcador1 = 0,
                        marcador2 = 0,
                        fase = obtenerFaseDelTorneo(partidos.size + 1),
                        idTorneo = torneo.id!!
                    )
                    partidos.add(partido)
                }
            }
            return partidos
        }

        private fun obtenerFaseDelTorneo(numeroPartidos: Int): String {
            return when (numeroPartidos) {
                in 1..4 -> "Cuartos de final"
                in 5..6 -> "Semifinales"
                7 -> "Final"
                else -> "Fase desconocida"
            }
        }

        private fun guardarPartidosEnBaseDeDatos(partidos: List<Partido>, idTorneo: String) {
            val database = FirebaseDatabase.getInstance().reference.child("partidos")
            for (partido in partidos) {
                database.child(partido.id).setValue(partido)
            }
        }



    }
}