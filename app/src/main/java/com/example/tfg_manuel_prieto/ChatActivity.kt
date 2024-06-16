package com.example.tfg_manuel_prieto

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Chat>()
    private lateinit var torneoId: String
    private lateinit var torneoNombre: String
    private lateinit var recyclerViewChat: RecyclerView
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        recyclerViewChat = findViewById(R.id.recycler_view_chat)
        editTextMessage = findViewById(R.id.edit_text_message)
        buttonSend = findViewById(R.id.button_send)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        torneoId = intent.getStringExtra("torneoId") ?: ""
        torneoNombre = intent.getStringExtra("torneoNombre") ?: ""
        chatAdapter = ChatAdapter(messageList, this@ChatActivity)
        recyclerViewChat.adapter = chatAdapter
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        recyclerViewChat.layoutManager = layoutManager
        recyclerViewChat.adapter = chatAdapter
        buttonSend.setOnClickListener {
            val message = editTextMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                enviarMensaje(message, torneoId)
                editTextMessage.text.clear()
            } else {
                Toast.makeText(this@ChatActivity, "Escribe un mensaje antes de enviar", Toast.LENGTH_SHORT).show()
            }
        }

        cargarMensajes()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun enviarMensaje(message: String, torneoId: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val usersRef = database.child("Users").child(userId)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val userName = dataSnapshot.child("nombre").getValue(String::class.java)
                    if (userName != null) {
                        val chatRef = database.child("chats").child(torneoId).push()
                        val idMensaje = chatRef.key ?: ""
                        val chatMessage = Chat(userId, userName, message, torneoId, idMensaje)
                        chatRef.setValue(chatMessage)
                            .addOnSuccessListener {
                                obtenerNombreTorneoYEnviarNotificaciones(userId, userName, torneoId)
                            }
                            .addOnFailureListener { exception ->
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error al enviar el mensaje: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun obtenerNombreTorneoYEnviarNotificaciones(userId: String, userName: String, torneoId: String) {
        val torneoRef = database.child("torneos").child(torneoId)
        torneoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val nombreTorneo = dataSnapshot.child("nombre").getValue(String::class.java) ?: ""
                enviarNotificacionesUsuariosExceptoYo(userId, userName, torneoId, nombreTorneo)
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun enviarNotificacionesUsuariosExceptoYo(userId: String, userName: String, torneoId: String, nombreTorneo: String) {
        val equiposRef = database.child("equipos")
        val query = equiposRef.orderByChild("idTorneo").equalTo(torneoId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val inscritos = mutableListOf<String>()

                for (equipoSnapshot in dataSnapshot.children) {
                    val idCapitan = equipoSnapshot.child("idCapitan").getValue(String::class.java)
                    val equipoId = equipoSnapshot.key ?: continue

                    if (idCapitan != userId) {
                        inscritos.add(idCapitan!!)
                    }

                    val miembrosRef = database.child("equipos").child(equipoId).child("miembros")
                    miembrosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(membersSnapshot: DataSnapshot) {
                            for (memberSnapshot in membersSnapshot.children) {
                                val memberId = memberSnapshot.key ?: continue
                                if (memberId != userId) {
                                    inscritos.add(memberId)
                                }
                            }
                            enviarNotificacion(userName, inscritos, torneoId, nombreTorneo)
                        }

                        override fun onCancelled(databaseError: DatabaseError) {
                        }
                    })
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun enviarNotificacion(userName: String, userIds: List<String>, torneoId: String, nombreTorneo: String) {
        userIds.forEach { userId ->
            val notificationId = database.child("notificaciones").child(userId).push().key ?: return
            val newNotification = Notificacion(
                titulo = "Nuevo mensaje en un chat de torneo",
                cuerpo = "Nuevo mensaje de $userName en el chat del torneo $nombreTorneo",
                leido = false,
                chatId = "",
                nombreTorneo = nombreTorneo,
                nombreUsuario = userName,
                userId = userId
            )

            database.child("notificaciones").child(userId).child(notificationId).setValue(newNotification)
                .addOnSuccessListener {
                // Notificación enviada correctamente
                }
                .addOnFailureListener { exception ->
                }
        }
    }

    private fun cargarMensajes() {
        val chatRef = database.child("chats").child(torneoId)
        chatRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(Chat::class.java)
                if (chatMessage != null) {
                    messageList.add(chatMessage)
                    chatAdapter.notifyItemInserted(messageList.size - 1)
                    recyclerViewChat.scrollToPosition(messageList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun reportarMensaje(idMensaje: String, motivo: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val chatsRef = database.child("chats")

            chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var mensajeEncontrado = false
                    var mensajeTexto: String? = null

                    for (torneoSnapshot in dataSnapshot.children) {
                        for (chatSnapshot in torneoSnapshot.children) {
                            val mensajeId = chatSnapshot.child("idMensaje").getValue(String::class.java)
                            val texto = chatSnapshot.child("message").getValue(String::class.java)
                            if (mensajeId == idMensaje) {
                                mensajeEncontrado = true
                                mensajeTexto = texto
                                break
                            }
                        }
                        if (mensajeEncontrado) break
                    }

                    if (mensajeEncontrado) {
                        val reporteRef = database.child("reportes").push()
                        val reporte = Reporte(
                            reporteId = reporteRef.key ?: "",
                            messageId = idMensaje,
                            userId = userId,
                            motivo = motivo,
                            mensaje = mensajeTexto ?: "",
                            resuelto = false
                        )
                        reporteRef.setValue(reporte)
                            .addOnSuccessListener {
                                Toast.makeText(this@ChatActivity, "Mensaje reportado correctamente", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(this@ChatActivity, "Error al reportar el mensaje: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(this@ChatActivity, "El mensaje no existe o ya ha sido eliminado", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@ChatActivity, "Error al buscar el mensaje: ${databaseError.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

}

