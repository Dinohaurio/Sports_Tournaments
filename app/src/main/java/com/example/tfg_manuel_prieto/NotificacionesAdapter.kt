package com.example.tfg_manuel_prieto

import com.example.tfg_manuel_prieto.Notificacion
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotificacionesAdapter(private var notificaciones: List<Notificacion>) : RecyclerView.Adapter<NotificacionesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notificacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notificacion = notificaciones[position]
        holder.tvTitulo.text = notificacion.titulo

        // Mostrar el cuerpo de la notificación con el nombre del usuario y torneo
        holder.tvCuerpo.text = "Mensaje de ${notificacion.nombreUsuario} en el torneo ${notificacion.nombreTorneo}"
    }

    override fun getItemCount(): Int {
        return notificaciones.size
    }

    fun actualizarNotificaciones(nuevasNotificaciones: List<Notificacion>) {
        notificaciones = nuevasNotificaciones
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitulo: TextView = itemView.findViewById(R.id.tvTitulo)
        val tvCuerpo: TextView = itemView.findViewById(R.id.tvCuerpo)
    }
}
