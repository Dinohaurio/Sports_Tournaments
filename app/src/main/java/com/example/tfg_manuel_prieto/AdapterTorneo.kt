package com.example.tfg_manuel_prieto

import Torneo
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdapterTorneo(private val torneos: List<Torneo>) : RecyclerView.Adapter<AdapterTorneo.TorneoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_torneo, parent, false)
        return TorneoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
        val torneo = torneos[position]
        holder.tvNombreTorneo.text = torneo.nombre
        holder.tvLocalizacion.text = torneo.localidad
        holder.tvDeporte.text = torneo.deporte
    }

    override fun getItemCount(): Int {
        return torneos.size
    }

    class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreTorneo: TextView = itemView.findViewById(R.id.tvNombreTorneo)
        val tvLocalizacion: TextView = itemView.findViewById(R.id.tvLocalidad)
        val tvDeporte: TextView = itemView.findViewById(R.id.tvDeporte)
    }
}