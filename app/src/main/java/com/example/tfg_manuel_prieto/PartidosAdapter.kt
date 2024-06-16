package com.example.tfg_manuel_prieto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PartidosAdapter(private var partidosList: List<Partido>) :
    RecyclerView.Adapter<PartidosAdapter.PartidoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartidoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_partido, parent, false)
        return PartidoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: PartidoViewHolder, position: Int) {
        val partido = partidosList[position]
        holder.bind(partido)
    }

    override fun getItemCount() = partidosList.size

    fun actualizarLista(partidos: List<Partido>) {
        partidosList = partidos.sortedWith(compareBy { obtenerPrioridadFase(it.fase) })
        notifyDataSetChanged()
    }

    private fun obtenerPrioridadFase(fase: String): Int {
        return when (fase) {
            "Dieciseisavos" -> 1
            "Octavos de final" -> 2
            "Cuartos de final" -> 3
            "Semifinales" -> 4
            "Final" -> 5
            else -> 6
        }
    }

    inner class PartidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val equipo1TextView: TextView = itemView.findViewById(R.id.equipo1)
        private val equipo2TextView: TextView = itemView.findViewById(R.id.equipo2)
        private val faseTextView: TextView = itemView.findViewById(R.id.fase)

        fun bind(partido: Partido) {
            equipo1TextView.text = partido.equipo1
            equipo2TextView.text = partido.equipo2
            faseTextView.text = partido.fase
        }
    }
}
