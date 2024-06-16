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
        buttonAdmin = findViewById(R.id.buttonAdmin) // Inicializar el botón de administración
        inicializar()
        mostrarMensajeBienvenida()
        configurarListeners()
        setupNotificationListener()
        verificarAdmin() // Verificar si el usuario es administrador
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
                notificationIcon.setColorFilter(null) // Clear the red color
                hasNotifications = false
            }
        }
    }

    private fun cerrarSesion() {
        auth.signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish() // Finaliza LobbyActivity para que no se pueda volver atrás a ella
    }

    private fun setupNotificationListener() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        database.child("notificaciones").child(userId).orderByChild("leido").equalTo(false)
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        hasNotifications = true
                        notificationBadge.visibility = View.VISIBLE // Mostrar el badge de notificaciones
                    }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if (snapshot.exists()) {
                        hasNotifications = true
                        notificationBadge.visibility = View.VISIBLE // Mostrar el badge de notificaciones
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    checkForUnreadNotifications(userId)
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    private fun checkForUnreadNotifications(userId: String) {
        database.child("notificaciones").child(userId).orderByChild("leido").equalTo(false)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    hasNotifications = dataSnapshot.exists()
                    notificationBadge.visibility = if (hasNotifications) View.VISIBLE else View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Handle error
                }
            })
    }

    // Función para manejar el clic en el icono de notificación
    fun onNotificationIconClick(view: View) {
        // Implementar la lógica para abrir la pantalla de mensajes nuevos
        // Por ejemplo, iniciar una nueva actividad
        startActivity(Intent(this, NotificacionesActivity::class.java))

        // Después de abrir la actividad, actualizar el estado de las notificaciones
        hasNotifications = false

        // Actualizar la visibilidad del badge
        notificationBadge.visibility = View.GONE
    }
}
