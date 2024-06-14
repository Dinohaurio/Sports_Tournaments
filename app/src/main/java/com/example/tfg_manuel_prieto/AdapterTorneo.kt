package com.example.tfg_manuel_prieto

import Torneo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
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

class AdapterTorneo(
    private val torneosList: List<Torneo>,
    private val onVerPartidosClicked: (Torneo) -> Unit // Nuevo listener para "Ver Partidos"
) : RecyclerView.Adapter<AdapterTorneo.TorneoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_torneo, parent, false)
        return TorneoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
        val torneo = torneosList[position]
        holder.bind(torneo)
    }

    override fun getItemCount(): Int = torneosList.size

    inner class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val nombreTorneo: TextView = itemView.findViewById(R.id.tvNombreTorneo)
        private val localidad: TextView = itemView.findViewById(R.id.tvLocalidad)
        private val deporte: TextView = itemView.findViewById(R.id.tvDeporte)
        private val btnGenerarPartidos: Button = itemView.findViewById(R.id.btnGenerarPartidos)
        private val btnVerPartidos: Button = itemView.findViewById(R.id.btnVerPartidos) // Nuevo botón

        fun bind(torneo: Torneo) {
            nombreTorneo.text = torneo.nombre
            localidad.text = "Localidad: ${torneo.localidad}"
            deporte.text = "Deporte: ${torneo.deporte}"

            val database = FirebaseDatabase.getInstance().reference
            val partidosRef = database.child("partidos").orderByChild("idTorneo").equalTo(torneo.id)

            partidosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        btnGenerarPartidos.visibility = View.GONE
                        btnVerPartidos.visibility = View.VISIBLE
                    } else {
                        btnGenerarPartidos.visibility = View.VISIBLE
                        btnVerPartidos.visibility = View.GONE
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si la consulta se cancela
                    Toast.makeText(itemView.context, "Error al verificar los partidos", Toast.LENGTH_SHORT).show()
                }
            })

            btnGenerarPartidos.setOnClickListener {
                generarYMostrarPartidos(torneo)
            }

            btnVerPartidos.setOnClickListener {
                onVerPartidosClicked(torneo)
            }
        }

        private fun generarYMostrarPartidos(torneo: Torneo) {
            val database = FirebaseDatabase.getInstance().reference
            val query = database.child("partidos").orderByChild("idTorneo").equalTo(torneo.id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.exists()) {
                        val fechaFinInscripcion = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(torneo.fechaFinInscripcion)
                        val fechaHoy = Calendar.getInstance().time
                        if (fechaHoy > fechaFinInscripcion) {
                            obtenerEquiposInscritosAlTorneo(torneo) { equipos ->
                                val partidos = generarPartidos(equipos, torneo)
                                guardarPartidosEnBaseDeDatos(partidos, torneo.id!!)
                            }
                        } else {
                            Toast.makeText(itemView.context, "Aún no se puede generar partidos", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(itemView.context, "Ya se han generado partidos para este torneo", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si la consulta se cancela
                    Toast.makeText(itemView.context, "Error al generar los partidos", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun obtenerEquiposInscritosAlTorneo(torneo: Torneo, callback: (List<Equipo>) -> Unit) {
            val equipos: MutableList<Equipo> = mutableListOf()
            val database = FirebaseDatabase.getInstance().reference
            val query = database.child("equipos").orderByChild("idTorneo").equalTo(torneo.id)

            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (equipoSnapshot in dataSnapshot.children) {
                        val equipo = equipoSnapshot.getValue(Equipo::class.java)
                        equipo?.let { equipos.add(it) }
                    }
                    callback(equipos)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Manejar errores si la consulta se cancela
                    Toast.makeText(itemView.context, "Error al obtener los equipos inscritos", Toast.LENGTH_SHORT).show()
                }
            })
        }

        private fun generarPartidos(equipos: List<Equipo>, torneo: Torneo): List<Partido> {
            val partidos = mutableListOf<Partido>()
            var equiposRestantes = equipos.toMutableList()

            if (equiposRestantes.size % 2 != 0) {
                equiposRestantes.add(Equipo(id = "bye", nombre = "Bye", idTorneo = torneo.id!!))
            }

            var partidoCount = 1

            while (equiposRestantes.size > 1) {
                val faseActual = obtenerFaseDelTorneo(equiposRestantes.size / 2)
                val partidosDeFase = mutableListOf<Partido>()

                for (i in equiposRestantes.indices step 2) {
                    val equipo1 = equiposRestantes[i]
                    val equipo2 = equiposRestantes[i + 1]
                    val partido = Partido(
                        id = UUID.randomUUID().toString(),
                        equipo1 = equipo1.nombre,
                        equipo2 = equipo2.nombre,
                        marcador1 = 0,
                        marcador2 = 0,
                        fase = faseActual,
                        idTorneo = torneo.id!!
                    )
                    partidos.add(partido)
                    partidosDeFase.add(partido)
                    partidoCount++
                }
                equiposRestantes = partidosDeFase.mapIndexed { index, _ ->
                    Equipo(
                        id = "ganadorPartido${index + 1}",
                        nombre = "Ganador Partido ${index + 1}",
                        idTorneo = torneo.id!!
                    )
                }.toMutableList()
            }

            return partidos
        }

        private fun obtenerFaseDelTorneo(numPartidos: Int): String {
            return when (numPartidos) {
                16 -> "Dieciseisavos"
                8 -> "Octavos de final"
                4 -> "Cuartos de final"
                2 -> "Semifinales"
                1 -> "Final"
                else -> "Fase desconocida"
            }
        }

        private fun guardarPartidosEnBaseDeDatos(partidos: List<Partido>, idTorneo: String) {
            val database = FirebaseDatabase.getInstance().reference.child("partidos")
            for (partido in partidos) {
                database.child(partido.id).setValue(partido)
            }
            Toast.makeText(itemView.context, "Partidos generados exitosamente", Toast.LENGTH_SHORT).show()
        }
    }
}