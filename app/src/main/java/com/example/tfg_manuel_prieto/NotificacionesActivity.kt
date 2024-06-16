package com.example.tfg_manuel_prieto

import android.annotation.SuppressLint
import android.app.Notification
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NotificacionesActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificacionesAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notificaciones)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        recyclerView = findViewById(R.id.recycler_notificaciones)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificacionesAdapter(emptyList()) // Inicializamos con lista vacía
        recyclerView.adapter = adapter

        cargarNotificaciones()
    }

    private fun cargarNotificaciones() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val notificacionesRef = database.child("notificaciones").child(userId)
        notificacionesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val notificaciones = mutableListOf<Notificacion>()

                for (notificacionSnapshot in dataSnapshot.children) {
                    val notificacion = notificacionSnapshot.getValue(Notificacion::class.java)
                    notificacion?.let {
                        notificaciones.add(it)
                    }
                }

                adapter.actualizarNotificaciones(notificaciones)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si es necesario
            }
        })
    }
}