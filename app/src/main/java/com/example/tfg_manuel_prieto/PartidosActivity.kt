package com.example.tfg_manuel_prieto

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PartidosActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var partidosAdapter: PartidosAdapter
    private lateinit var database: DatabaseReference
    private var partidosList = mutableListOf<Partido>()
    private lateinit var torneoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        recyclerView = findViewById(R.id.recyclerViewPartidos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        partidosAdapter = PartidosAdapter(partidosList)
        recyclerView.adapter = partidosAdapter

        database = FirebaseDatabase.getInstance().reference
        torneoId = intent.getStringExtra("torneoId") ?: ""

        obtenerPartidos()
    }

    private fun obtenerPartidos() {
        val partidosRef = database.child("partidos").orderByChild("idTorneo").equalTo(torneoId)

        partidosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    partidosList.clear()
                    for (partidoSnapshot in dataSnapshot.children) {
                        val partido = partidoSnapshot.getValue(Partido::class.java)
                        partido?.let { partidosList.add(it) }
                    }
                    // Actualizar el adaptador con la lista de partidos obtenida
                    partidosAdapter.actualizarLista(partidosList)
                } else {
                    // Manejar caso donde no hay partidos encontrados
                    Toast.makeText(this@PartidosActivity, "No se encontraron partidos", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Manejar errores si la consulta se cancela
                Toast.makeText(this@PartidosActivity, "Error al obtener los partidos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}