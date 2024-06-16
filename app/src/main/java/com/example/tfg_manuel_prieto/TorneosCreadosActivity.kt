package com.example.tfg_manuel_prieto

import Torneo
import android.app.Notification
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.WindowInsetsAnimation
import android.widget.Toast
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
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TorneosCreadosActivity : AppCompatActivity() {

    private lateinit var recyclerViewTorneos: RecyclerView
    private lateinit var torneoAdapter: AdapterTorneo
    private lateinit var torneosList: MutableList<Torneo>
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var usuarioId: String? = null // Variable para almacenar el ID del usuario actual

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_torneos_creados)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        recyclerViewTorneos = findViewById(R.id.recyclerViewTorneos)
        recyclerViewTorneos.layoutManager = LinearLayoutManager(this)

        torneosList = mutableListOf()
        torneoAdapter = AdapterTorneo(torneosList) { torneo ->
            irAPantallaDePartidos(torneo)
        }
        recyclerViewTorneos.adapter = torneoAdapter

        cargarUsuarioYTorneos()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarUsuarioYTorneos() {
        val currentUser = auth.currentUser
        usuarioId = currentUser?.uid

        if (usuarioId != null) {
            cargarTorneosDelUsuario()
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cargarTorneosDelUsuario() {
        val torneosRef = database.child("torneos").orderByChild("usuarioId").equalTo(usuarioId)

        // Usamos addChildEventListener para escuchar cambios en la lista de torneos
        torneosRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val torneo = snapshot.getValue(Torneo::class.java)
                if (torneo != null) {
                    torneosList.add(torneo)
                    torneoAdapter.notifyItemInserted(torneosList.size - 1)
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                // Manejar cambios en los torneos si es necesario
                val torneoChanged = snapshot.getValue(Torneo::class.java)
                val index = torneosList.indexOfFirst { it.id == torneoChanged?.id }
                if (index != -1 && torneoChanged != null) {
                    torneosList[index] = torneoChanged
                    torneoAdapter.notifyItemChanged(index)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Manejar eliminaciones de torneos si es necesario
                val torneoRemoved = snapshot.getValue(Torneo::class.java)
                val index = torneosList.indexOfFirst { it.id == torneoRemoved?.id }
                if (index != -1 && torneoRemoved != null) {
                    torneosList.removeAt(index)
                    torneoAdapter.notifyItemRemoved(index)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                // Implementar si se espera movimiento de torneos
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@TorneosCreadosActivity, "Error al cargar los torneos", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun irAPantallaDePartidos(torneo: Torneo) {
        val intent = Intent(this, PartidosActivity::class.java)
        intent.putExtra("torneoId", torneo.id)
        startActivity(intent)
    }


}