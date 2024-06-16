package com.example.tfg_manuel_prieto

import Torneo
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ModificarTorneoActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var etNombreTorneo: EditText
    private lateinit var etLocalidad: EditText
    private lateinit var etParticipantes: EditText
    private lateinit var etFechaInicioInscripcion: EditText
    private lateinit var etFechaFinInscripcion: EditText
    private lateinit var etFechaComienzo: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var spinnerDeporte: Spinner
    private lateinit var btnGuardarCambios: Button
    private lateinit var torneoId: String
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private var nuevaLatitud: String = ""
    private var nuevaLongitud: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modificar_torneo)
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
        torneoId = intent.getStringExtra("torneoId") ?: ""
        etNombreTorneo = findViewById(R.id.etNombreTorneo)
        etLocalidad = findViewById(R.id.etLocalidad)
        etParticipantes = findViewById(R.id.etParticipantes)
        etFechaInicioInscripcion = findViewById(R.id.etFechaInicioInscripcion)
        etFechaFinInscripcion = findViewById(R.id.etFechaFinInscripcion)
        etFechaComienzo = findViewById(R.id.etFechaComienzo)
        etFechaFin = findViewById(R.id.etFechaFin)
        spinnerDeporte = findViewById(R.id.spinnerDeporte)
        btnGuardarCambios = findViewById(R.id.btnGuardarCambios)
        mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        btnGuardarCambios.setOnClickListener {
            guardarCambios()
        }

        cargarDatosTorneo()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val torneoRef = database.child("torneos").child(torneoId)
        torneoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val torneo = dataSnapshot.getValue(Torneo::class.java)
                    torneo?.let {
                        val latitud = it.latitud?.toDoubleOrNull()
                        val longitud = it.longitud?.toDoubleOrNull()
                        if (latitud != null && longitud != null) {
                            val ubicacionTorneo = LatLng(latitud, longitud)
                            mMap.addMarker(MarkerOptions().position(ubicacionTorneo).title("Ubicación del Torneo"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacionTorneo, 15f))
                            nuevaLatitud = latitud.toString()
                            nuevaLongitud = longitud.toString()
                        } else {
                            Toast.makeText(this@ModificarTorneoActivity, "La ubicación del torneo no está definida correctamente", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@ModificarTorneoActivity, "No se encontró el torneo", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ModificarTorneoActivity, "Error al cargar el torneo", Toast.LENGTH_SHORT).show()
                finish()
            }
        })

        mMap.setOnMapClickListener { latLng ->
            mMap.clear()
            mMap.addMarker(MarkerOptions().position(latLng).title("Ubicación del Torneo"))
            nuevaLatitud = latLng.latitude.toString()
            nuevaLongitud = latLng.longitude.toString()
        }
    }

    private fun cargarDatosTorneo() {
        val torneoRef = database.child("torneos").child(torneoId)

        torneoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val torneo = dataSnapshot.getValue(Torneo::class.java)
                    torneo?.let {
                        etNombreTorneo.setText(it.nombre)
                        etLocalidad.setText(it.localidad)
                        etParticipantes.setText(it.participantes.toString())
                        etFechaInicioInscripcion.setText(it.fechaComienzoInscripcion)
                        etFechaFinInscripcion.setText(it.fechaFinInscripcion)
                        etFechaComienzo.setText(it.fechaComienzo)
                        etFechaFin.setText(it.fechaFin)
                        cargarDeportesEnSpinner(it.deporte)
                    }
                } else {
                    Toast.makeText(this@ModificarTorneoActivity, "No se encontró el torneo", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ModificarTorneoActivity, "Error al cargar el torneo", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }

    private fun cargarDeportesEnSpinner(deporteSeleccionado: String?) {
        val deportesRef = database.child("Deportes")
        deportesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val deportesList = mutableListOf<String>()
                    for (snapshot in dataSnapshot.children) {
                        val deporte = snapshot.value as String
                        deportesList.add(deporte)
                    }

                    val adapter = ArrayAdapter(this@ModificarTorneoActivity, android.R.layout.simple_spinner_item, deportesList)
                    spinnerDeporte.adapter = adapter

                    deporteSeleccionado?.let {
                        val selectedPosition = deportesList.indexOf(it)
                        spinnerDeporte.setSelection(selectedPosition)
                    }
                } else {
                    Toast.makeText(this@ModificarTorneoActivity, "No se encontraron deportes disponibles", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@ModificarTorneoActivity, "Error al cargar los deportes", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun guardarCambios() {
        val nombre = etNombreTorneo.text.toString().trim()
        val localidad = etLocalidad.text.toString().trim()
        val participantes = etParticipantes.text.toString().toIntOrNull()
        val fechaInicioInscripcion = etFechaInicioInscripcion.text.toString().trim()
        val fechaFinInscripcion = etFechaFinInscripcion.text.toString().trim()
        val fechaComienzo = etFechaComienzo.text.toString().trim()
        val fechaFin = etFechaFin.text.toString().trim()
        val deporte = spinnerDeporte.selectedItem.toString()

        if (nombre.isEmpty() || localidad.isEmpty() || participantes == null ||
            fechaInicioInscripcion.isEmpty() || fechaFinInscripcion.isEmpty() ||
            fechaComienzo.isEmpty() || fechaFin.isEmpty() || deporte.isEmpty() ||
            nuevaLatitud.isEmpty() || nuevaLongitud.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos y establece la ubicación del torneo", Toast.LENGTH_SHORT).show()
            return
        }

        val torneoActualizado = Torneo(
            torneoId,
            nombre,
            localidad,
            participantes,
            fechaInicioInscripcion,
            fechaFinInscripcion,
            fechaComienzo,
            fechaFin,
            deporte,
            auth.currentUser?.uid,
            nuevaLatitud,
            nuevaLongitud
        )

        val torneoRef = database.child("torneos").child(torneoId)
        torneoRef.setValue(torneoActualizado)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Torneo actualizado correctamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Error al actualizar el torneo", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun mostrarDatePicker(editText: EditText) {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(Calendar.YEAR, year)
                selectedDate.set(Calendar.MONTH, month)
                selectedDate.set(Calendar.DAY_OF_MONTH, day)
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(format.format(selectedDate.time))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    fun onFechaInicioInscripcionClick(view: View) {
        mostrarDatePicker(etFechaInicioInscripcion)
    }

    fun onFechaFinInscripcionClick(view: View) {
        mostrarDatePicker(etFechaFinInscripcion)
    }

    fun onFechaComienzoClick(view: View) {
        mostrarDatePicker(etFechaComienzo)
    }

    fun onFechaFinClick(view: View) {
        mostrarDatePicker(etFechaFin)
    }
}