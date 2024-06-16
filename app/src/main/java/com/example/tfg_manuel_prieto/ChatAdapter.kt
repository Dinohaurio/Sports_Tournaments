package com.example.tfg_manuel_prieto

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(private val messageList: List<Chat>, private val chatActivity: ChatActivity) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageTextView: TextView = itemView.findViewById(R.id.message_text)
        val userTextView: TextView = itemView.findViewById(R.id.user_text)
        init {
            itemView.setOnLongClickListener {
                val chat = messageList[adapterPosition]
                Log.d(TAG, "ID Mensaje: ${chat.idMensaje}")
                mostrarDialogoReporte(chat.idMensaje) { idMensaje, motivo ->
                    chatActivity.reportarMensaje(idMensaje, motivo)
                }
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat, parent, false)
        return ChatViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chatMessage = messageList[position]
        holder.messageTextView.text = chatMessage.message
        holder.userTextView.text = chatMessage.userName
    }

    override fun getItemCount() = messageList.size

    fun mostrarDialogoReporte(idMensaje: String, reportarMensaje: (String, String) -> Unit) {
        val motivoEditText = EditText(chatActivity)
        motivoEditText.hint = "Motivo del reporte"

        val dialog = AlertDialog.Builder(chatActivity)
            .setTitle("Reportar mensaje")
            .setView(motivoEditText)
            .setPositiveButton("Reportar") { _, _ ->
                val motivo = motivoEditText.text.toString().trim()
                if (motivo.isNotEmpty()) {
                    reportarMensaje(idMensaje, motivo)
                } else {
                    Toast.makeText(chatActivity, "Ingresa un motivo para el reporte", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .create()
        dialog.show()
    }
}