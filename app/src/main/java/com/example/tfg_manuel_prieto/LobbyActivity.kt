package com.example.tfg_manuel_prieto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LobbyActivity : AppCompatActivity() {
    private lateinit var tvBienvenido: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lobby_activity)
        inicializar()
        mostrarMensajeBienvenida()
        configurarListeners()
    }

    private fun inicializar() {
        tvBienvenido = findViewById(R.id.tvBienvenido)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    private fun mostrarMensajeBienvenida() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val usersRef = database.child("Users").child(userId)

            usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val nombreUsuario = dataSnapshot.child("nombre").getValue(String::class.java)

                    if (nombreUsuario != null) {
                        tvBienvenido.visibility = TextView.VISIBLE
                        tvBienvenido.text = "¡Bienvenido, $nombreUsuario!"
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@LobbyActivity, "Error al obtener el usuario", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


    private fun configurarListeners() {
        // Configura los listeners de clic para cada botón
        findViewById<Button>(R.id.button1).setOnClickListener {
            startActivity(Intent(this, CrearTorneoActivity::class.java))
        }

        findViewById<Button>(R.id.button2).setOnClickListener {
            startActivity(Intent(this, BuscarTorneoActivity::class.java))
        }

        findViewById<Button>(R.id.button3).setOnClickListener {
            startActivity(Intent(this, TorneosCreadosActivity::class.java))
        }

        findViewById<Button>(R.id.button4).setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        findViewById<Button>(R.id.button5).setOnClickListener {
            startActivity(Intent(this, TorneosParticipadosActivity::class.java))
        }

        findViewById<Button>(R.id.button6).setOnClickListener {
            cerrarSesion()
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finaliza LobbyActivity para que no se pueda volver atrás a ella
    }
}