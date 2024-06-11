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
    private val partidosList = mutableListOf<Partido>()
    private lateinit var torneoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_partidos)

        recyclerView = findViewById(R.id.recyclerViewPartidos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        partidosAdapter = PartidosAdapter(partidosList)
        recyclerView.adapter = partidosAdapter

        database = FirebaseDatabase.getInstance().reference
        torneoId = intent.getStringExtra("TORNEO_ID") ?: ""

        fetchPartidos()
    }

    private fun fetchPartidos() {
        val partidosRef = database.child("partidos").orderByChild("idTorneo").equalTo(torneoId)

        partidosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val partido = snapshot.getValue(Partido::class.java)
                    partido?.let { partidosList.add(it) }
                }
                partidosAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@PartidosActivity, "Error al obtener los partidos", Toast.LENGTH_SHORT).show()
            }
        })
    }


}