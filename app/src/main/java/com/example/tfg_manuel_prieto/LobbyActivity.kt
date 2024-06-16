package com.example.tfg_manuel_prieto

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LobbyActivity : AppCompatActivity() {
    private lateinit var tvBienvenido: TextView
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var notificationIcon: ImageView
    private lateinit var notificationBadge: ImageView
    private lateinit var buttonAdmin: Button // Declarar el botón de administración
    private var hasNotifications = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lobby_activity)
        notificationIcon = findViewById(R.id.notification_icon)
        notificationBadge = findViewById(R.id.notification_badge)
        buttonAdmin = findViewById(R.id.buttonAdmin)
        inicializar()
        mostrarMensajeBienvenida()
        configurarListeners()
        setupNotificationListener()
        verificarAdmin()
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

    private fun verificarAdmin() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val email = currentUser.email ?: ""
            if (email == "prirodmanuel@gmail.com") {
                buttonAdmin.visibility = View.VISIBLE // Mostrar el botón si es administrador
            }
        }
    }

    private fun configurarListeners() {
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
            startActivity(Intent(this, ListaChatsActivity::class.java))
        }

        findViewById<Button>(R.id.button5).setOnClickListener {
            startActivity(Intent(this, TorneosParticipadosActivity::class.java))
        }

        findViewById<Button>(R.id.button6).setOnClickListener {
            cerrarSesion()
        }

        buttonAdmin.setOnClickListener {
            startActivity(Intent(this, LobbyAdminActivity::class.java))
        }

        notificationIcon.setOnClickListener {
            val intent = Intent(this, NotificacionesActivity::class.java)
            startActivity(intent)
            if (hasNotifications) {
                notificationIcon.setColorFilter(null)
                hasNotifications = false
            }
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun setupNotificationListener() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        database.child("notificaciones").child(userId).orderByChild("leido").equalTo(false)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        hasNotifications = true
                        notificationBadge.visibility = View.VISIBLE
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        hasNotifications = true
                        notificationBadge.visibility = View.VISIBLE
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    notificaiconesNoLeidas(userId)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun notificaiconesNoLeidas(userId: String) {
        database.child("notificaciones").child(userId).orderByChild("leido").equalTo(false)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    hasNotifications = dataSnapshot.exists()
                    notificationBadge.visibility = if (hasNotifications) View.VISIBLE else View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
    }

    fun onNotificationIconClick(view: View) {
        startActivity(Intent(this, NotificacionesActivity::class.java))
        hasNotifications = false
        notificationBadge.visibility = View.GONE
    }
}
