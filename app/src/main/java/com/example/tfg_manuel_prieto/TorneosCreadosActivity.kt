package com.example.tfg_manuel_prieto

import Torneo
import android.os.Bundle
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TorneosCreadosActivity : AppCompatActivity() {

    private lateinit var recyclerViewTorneos: RecyclerView
    private lateinit var torneoAdapter: AdapterTorneo
    private lateinit var torneosList: MutableList<Torneo>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torneos_creados)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerViewTorneos = findViewById(R.id.recyclerViewTorneos)
        recyclerViewTorneos.layoutManager = LinearLayoutManager(this)

        torneosList = mutableListOf()
        torneoAdapter = AdapterTorneo(torneosList)
        recyclerViewTorneos.adapter = torneoAdapter

        cargarTorneos()
    }

    private fun cargarTorneos() {
        val currentUser = auth.currentUser
        val usuarioId = currentUser?.uid
        val hoy = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        if (usuarioId != null) {
            val torneosRef = database.child("torneos").orderByChild("usuarioId").equalTo(usuarioId)

            torneosRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    torneosList.clear()
                    for (snapshot in dataSnapshot.children) {
                        val torneo = snapshot.getValue(Torneo::class.java)
                        if (torneo != null && torneo.fechaFin!! >= hoy) {
                            torneosList.add(torneo)
                        }
                    }
                    torneoAdapter.notifyDataSetChanged()
                }
                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@TorneosCreadosActivity, "Error al cargar los torneos", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }
}