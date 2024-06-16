package com.example.tfg_manuel_prieto

import Torneo
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListaChatsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var recyclerView: RecyclerView
    private lateinit var torneosAdapter: TorneosAdapter
    private val torneosList = mutableListOf<Torneo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lista_chat_torneo)

        recyclerView = findViewById(R.id.recycler_view_torneos)
        recyclerView.layoutManager = LinearLayoutManager(this)
        torneosAdapter = TorneosAdapter(torneosList)
        recyclerView.adapter = torneosAdapter

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        cargarTorneos()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarTorneos() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            val torneosRef = database.child("torneos")
            val equiposRef = database.child("equipos")

            // Consultar torneos creados por el usuario actual
            val torneosCreadosQuery = torneosRef.orderByChild("usuarioId").equalTo(userId)
            torneosCreadosQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val torneo = snapshot.getValue(Torneo::class.java)
                        if (torneo != null) {
                            torneosList.add(torneo)
                        }
                    }
                    torneosAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                }
            })

            // Consultar torneos en los que el usuario actual está inscrito como participante
            val torneosInscritoQuery = equiposRef.orderByChild("idCapitan").equalTo(userId)
            torneosInscritoQuery.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        val idTorneo = snapshot.child("idTorneo").getValue(String::class.java)
                        if (idTorneo != null) {
                            torneosRef.child(idTorneo).addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(torneoSnapshot: DataSnapshot) {
                                    val torneo = torneoSnapshot.getValue(Torneo::class.java)
                                    if (torneo != null && !torneosList.any { it.id == torneo.id }) {
                                        torneosList.add(torneo)
                                    }
                                    torneosAdapter.notifyDataSetChanged()
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    // Handle possible errors.
                                }
                            })
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle possible errors.
                }
            })
        }
    }

    inner class TorneosAdapter(private val torneosList: List<Torneo>) :
        RecyclerView.Adapter<TorneosAdapter.TorneoViewHolder>() {

        inner class TorneoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val torneoNameTextView: TextView = itemView.findViewById(R.id.torneo_name)

            init {
                itemView.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val torneo = torneosList[position]
                        val intent = Intent(this@ListaChatsActivity, ChatActivity::class.java)
                        intent.putExtra("torneoId", torneo.id)
                        startActivity(intent)
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TorneoViewHolder {
            val itemView = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_lista_chats, parent, false)
            return TorneoViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TorneoViewHolder, position: Int) {
            val torneo = torneosList[position]
            holder.torneoNameTextView.text = torneo.nombre
        }

        override fun getItemCount() = torneosList.size
    }
}