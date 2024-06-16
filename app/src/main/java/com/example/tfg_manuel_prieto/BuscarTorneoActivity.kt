package com.example.tfg_manuel_prieto

import Torneo
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.text.toLowerCase
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BuscarTorneoActivity : AppCompatActivity() {
    private lateinit var etLocalidad: EditText
    private lateinit var spinnerDeporte: Spinner
    private lateinit var btnBuscar: Button
    private lateinit var recyclerViewTorneos: RecyclerView
    private lateinit var torneoAdapter: BuscarTorneoAdapter
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torneos_buscados)

        inicializar()
        cargarDeportes()
    }

    private fun inicializar() {
        etLocalidad = findViewById(R.id.etLocalidad)
        spinnerDeporte = findViewById(R.id.spinnerDeporte)
        btnBuscar = findViewById(R.id.btnBuscar)
        recyclerViewTorneos = findViewById(R.id.recyclerViewTorneos)

        recyclerViewTorneos.layoutManager = LinearLayoutManager(this)
        torneoAdapter = BuscarTorneoAdapter(emptyList())
        recyclerViewTorneos.adapter = torneoAdapter

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        btnBuscar.setOnClickListener { buscarTorneos() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarDeportes() {
        val listaDeportes = mutableListOf<String>()
        val deportesRef = database.child("Deportes")

        deportesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val deporte = snapshot.getValue(String::class.java)
                    if (deporte != null) {
                        listaDeportes.add(deporte)
                    }
                }
                val adapter = ArrayAdapter(this@BuscarTorneoActivity, android.R.layout.simple_spinner_item, listaDeportes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDeporte.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BuscarTorneoActivity, "Error al cargar los deportes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun buscarTorneos() {
        val localidad = etLocalidad.text.toString().toLowerCase()
        val deporte = spinnerDeporte.selectedItem.toString()

        if (localidad.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa una localidad", Toast.LENGTH_SHORT).show()
            return
        }

        val torneosRef = database.child("torneos")

        torneosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val torneosList = mutableListOf<Torneo>()
                val hoy = Calendar.getInstance().time
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

                for (snapshot in dataSnapshot.children) {
                    val torneo = snapshot.getValue(Torneo::class.java)
                    if (torneo != null && torneo.localidad?.toLowerCase() == localidad && torneo.deporte == deporte) {
                        val fechaFinInscripcion = dateFormat.parse(torneo.fechaFinInscripcion)
                        if (fechaFinInscripcion != null && hoy.before(fechaFinInscripcion)) {
                            torneosList.add(torneo)
                        }
                    }
                }

                torneoAdapter.updateData(torneosList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@BuscarTorneoActivity, "Error al buscar los torneos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}