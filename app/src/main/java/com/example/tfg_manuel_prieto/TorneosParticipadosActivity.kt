package com.example.tfg_manuel_prieto

import Torneo
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TorneosParticipadosActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var torneosParticipadosAdapter: TorneosParticipadosAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private val torneosList = mutableListOf<Torneo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torneos_participados)

        recyclerView = findViewById(R.id.recyclerViewTorneosParticipados)
        recyclerView.layoutManager = LinearLayoutManager(this)
        torneosParticipadosAdapter = TorneosParticipadosAdapter(torneosList) { torneo ->
            // Handle partidos button click
            val intent = Intent(this, PartidosActivity::class.java)
            intent.putExtra("TORNEO_ID", torneo.id)
            startActivity(intent)
        }
        recyclerView.adapter = torneosParticipadosAdapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        fetchTorneosParticipados()
    }

    private fun fetchTorneosParticipados() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val equiposRef = database.child("equipos")

            equiposRef.orderByChild("idCapitan").equalTo(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        for (equipoSnapshot in dataSnapshot.children) {
                            val equipo = equipoSnapshot.getValue(Equipo::class.java)
                            equipo?.let { fetchTorneoPorId(it.idTorneo) }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Toast.makeText(this@TorneosParticipadosActivity, "Error al obtener los torneos participados", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    private fun fetchTorneoPorId(torneoId: String) {
        val torneosRef = database.child("torneos")

        torneosRef.child(torneoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val torneo = dataSnapshot.getValue(Torneo::class.java)
                    torneo?.let {
                        torneosList.add(it)
                        torneosParticipadosAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@TorneosParticipadosActivity, "Error al obtener el torneo", Toast.LENGTH_SHORT).show()
                }
            })
    }
}