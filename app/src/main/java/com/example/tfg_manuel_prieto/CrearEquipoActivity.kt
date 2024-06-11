package com.example.tfg_manuel_prieto

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CrearEquipoActivity: AppCompatActivity() {
    private lateinit var etNombreEquipo: EditText
    private lateinit var btnGuardar: Button
    private lateinit var tvCapitan: TextView
    private lateinit var tvNombreTorneo: TextView
    private lateinit var tvPlazasRestantes: TextView
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var torneoId: String
    private lateinit var torneoNombre: String
    private var maxParticipantes: Int = 0
    private var nombreUsuario: String? = null
    private val numerosTomados = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crear_equipo)

        torneoId = intent.getStringExtra("TORNEO_ID") ?: ""
        torneoNombre = intent.getStringExtra("TORNEO_NOMBRE") ?: ""
        maxParticipantes = intent.getIntExtra("MAX_PARTICIPANTES", 0)

        inicializar()
        obtenerNombreUsuario()
        verificarInscripcionPrevia()
    }

    private fun inicializar() {
        etNombreEquipo = findViewById(R.id.etNombreEquipo)
        btnGuardar = findViewById(R.id.btnGuardar)
        tvCapitan = findViewById(R.id.tvCapitan)
        tvNombreTorneo = findViewById(R.id.tvNombreTorneo)
        tvPlazasRestantes = findViewById(R.id.tvPlazasRestantes)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        tvNombreTorneo.text = "Torneo: $torneoNombre"

        btnGuardar.setOnClickListener { guardarEquipo() }
    }

    private fun cargarNumerosParticipantes() {
        val equiposRef = database.child("equipos")
        equiposRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                numerosTomados.clear()
                for (snapshot in dataSnapshot.children) {
                    val equipo = snapshot.getValue(Equipo::class.java)
                    if (equipo?.idTorneo == torneoId) {
                        equipo?.let {
                            numerosTomados.add(it.numeroParticipantes)
                        }
                    }
                }

                val plazasRestantes = maxParticipantes - numerosTomados.size
                tvPlazasRestantes.text = "Plazas restantes: $plazasRestantes"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@CrearEquipoActivity, "Error al cargar los números de participantes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun obtenerNombreUsuario() {
        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            val userId = usuarioActual.uid
            val userRef = database.child("Users").child(userId)

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    nombreUsuario = dataSnapshot.child("nombre").getValue(String::class.java)
                    if (nombreUsuario != null) {
                        tvCapitan.text = "Capitán: $nombreUsuario"
                    } else {
                        tvCapitan.text = "Capitán: Nombre no disponible"
                    }
                    // Cargar los números de participantes después de obtener el nombre del usuario
                    cargarNumerosParticipantes()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@CrearEquipoActivity, "Error al obtener el nombre del usuario", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            tvCapitan.text = "Capitán: Usuario no autenticado"
        }
    }

    private fun verificarInscripcionPrevia() {
        val usuarioActual = auth.currentUser
        if (usuarioActual != null) {
            val userId = usuarioActual.uid
            val equiposRef = database.child("equipos").orderByChild("idCapitan").equalTo(userId)

            equiposRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var inscrito = false
                    for (snapshot in dataSnapshot.children) {
                        val equipo = snapshot.getValue(Equipo::class.java)
                        if (equipo?.idTorneo == torneoId) {
                            inscrito = true
                            break
                        }
                    }

                    if (inscrito) {
                        Toast.makeText(this@CrearEquipoActivity, "Ya estás inscrito en este torneo", Toast.LENGTH_SHORT).show()
                        btnGuardar.isEnabled = false
                    } else {
                        // Solo cargar números de participantes si no está inscrito
                        cargarNumerosParticipantes()
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(this@CrearEquipoActivity, "Error al verificar la inscripción previa", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun guardarEquipo() {
        val nombreEquipo = etNombreEquipo.text.toString()
        val usuarioActual = auth.currentUser

        if (nombreEquipo.isEmpty() || usuarioActual == null) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        if (numerosTomados.size >= maxParticipantes) {
            Toast.makeText(this, "Ya no hay plazas disponibles para este torneo", Toast.LENGTH_SHORT).show()
            return
        }

        val equipoId = "${usuarioActual.uid}_${System.currentTimeMillis()}" // Genera una clave única para el equipo
        val nombreCapitan = nombreUsuario ?: "Capitán"

        val numeroParticipantes = if (numerosTomados.isEmpty()) {
            1
        } else {
            (numerosTomados.maxOrNull() ?: 0) + 1
        }

        val equipo = Equipo(
            id = equipoId,
            nombre = nombreEquipo,
            capitan = nombreCapitan,
            idCapitan = usuarioActual.uid,
            numeroParticipantes = numeroParticipantes,
            nombreTorneo = torneoNombre,
            idTorneo = torneoId
        )

        val equiposRef = database.child("equipos").child(equipoId)

        equiposRef.setValue(equipo).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this@CrearEquipoActivity, "Equipo creado con éxito", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@CrearEquipoActivity, "Error al crear el equipo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}