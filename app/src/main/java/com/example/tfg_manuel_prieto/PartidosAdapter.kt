package com.example.tfg_manuel_prieto

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PartidosAdapter(private val partidosList: List<Partido>) :
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

    inner class PartidoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val equipo1TextView: TextView = itemView.findViewById(R.id.equipo1)
        private val marcador1TextView: TextView = itemView.findViewById(R.id.marcador1)
        private val equipo2TextView: TextView = itemView.findViewById(R.id.equipo2)
        private val marcador2TextView: TextView = itemView.findViewById(R.id.marcador2)
        private val faseTextView: TextView = itemView.findViewById(R.id.fase)

        fun bind(partido: Partido) {
            equipo1TextView.text = partido.equipo1
            marcador1TextView.text = partido.marcador1.toString()
            equipo2TextView.text = partido.equipo2
            marcador2TextView.text = partido.marcador2.toString()
            faseTextView.text = partido.fase
        }
    }
}
