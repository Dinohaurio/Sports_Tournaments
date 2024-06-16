package com.example.tfg_manuel_prieto

import Torneo
import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CrearTorneoActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var etNombreTorneo: EditText
    private lateinit var etLocalidad: EditText
    private lateinit var etParticipantes: EditText
    private lateinit var etFechaComienzo: EditText
    private lateinit var etFechaFin: EditText
    private lateinit var etFechaInicioInscripcion: EditText
    private lateinit var etFechaFinInscripcion: EditText
    private lateinit var spinnerDeporte: Spinner
    private lateinit var btnGuardar: Button
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    private lateinit var mapa: GoogleMap
    private var torneoLatLng: LatLng? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_torneo)

        inicializar()
        cargarDeportes()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun inicializar() {
        etNombreTorneo = findViewById(R.id.etNombreTorneo)
        etLocalidad = findViewById(R.id.etLocalidad)
        etParticipantes = findViewById(R.id.etParticipantes)
        etFechaComienzo = findViewById(R.id.etFechaComienzo)
        etFechaFin = findViewById(R.id.etFechaFin)
        spinnerDeporte = findViewById(R.id.spinnerDeporte)
        btnGuardar = findViewById(R.id.btnGuardar)
        etFechaInicioInscripcion = findViewById(R.id.etFechaInicioInscripcion)
        etFechaFinInscripcion = findViewById(R.id.etFechaFinInscripcion)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        etFechaInicioInscripcion.setOnClickListener { mostrarDatePickerDialog(etFechaInicioInscripcion) }
        etFechaFinInscripcion.setOnClickListener { mostrarDatePickerDialog(etFechaFinInscripcion) }

        etFechaComienzo.setOnClickListener { mostrarDatePickerDialog(etFechaComienzo) }
        etFechaFin.setOnClickListener { mostrarDatePickerDialog(etFechaFin) }

        btnGuardar.setOnClickListener { guardarTorneo() }
    }

    private fun mostrarDatePickerDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                editText.setText(dateFormat.format(selectedDate.time))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
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
                val adapter = ArrayAdapter(this@CrearTorneoActivity, android.R.layout.simple_spinner_item, listaDeportes)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerDeporte.adapter = adapter
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@CrearTorneoActivity, "Error al cargar los deportes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mapa = googleMap

        mapa.setOnMapClickListener { latLng ->
            mapa.clear()
            mapa.addMarker(MarkerOptions().position(latLng).title("Ubicación del Torneo").draggable(true))
            torneoLatLng = latLng
        }

        mapa.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {}

            override fun onMarkerDrag(marker: Marker) {}

            override fun onMarkerDragEnd(marker: Marker) {
                torneoLatLng = marker.position
            }
        })

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            return
        }

        mapa.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
            } else {
                val defaultLocation = LatLng(40.416775, -3.703790)
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10f))
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mapa.isMyLocationEnabled = true
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLocation = LatLng(location.latitude, location.longitude)
                            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 18f))
                        } else {
                            val defaultLocation = LatLng(40.416775, -3.703790)
                            mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
                        }
                    }
                }
            } else {
                val defaultLocation = LatLng(40.416775, -3.703790)
                mapa.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 5f))
            }
        }
    }

    private fun guardarTorneo() {
        val nombre = etNombreTorneo.text.toString()
        val localidad = etLocalidad.text.toString()
        val participantes = etParticipantes.text.toString().toIntOrNull()
        val fechaInicioInscripcion = etFechaInicioInscripcion.text.toString()
        val fechaFinInscripcion = etFechaFinInscripcion.text.toString()
        val fechaComienzo = etFechaComienzo.text.toString()
        val fechaFin = etFechaFin.text.toString()
        val deporte = spinnerDeporte.selectedItem.toString()

        if (fechaInicioInscripcion.isEmpty() || fechaFinInscripcion.isEmpty() || !validarFechasInscripcion(fechaInicioInscripcion, fechaFinInscripcion)) {
            Toast.makeText(this, "Por favor, ingresa fechas válidas de inscripción", Toast.LENGTH_SHORT).show()
            return
        }

        if (nombre.isEmpty() || localidad.isEmpty() || participantes == null || fechaComienzo.isEmpty() || fechaFin.isEmpty() || deporte.isEmpty() || torneoLatLng == null) {
            Toast.makeText(this, "Por favor, rellena todos los campos y selecciona una ubicación en el mapa", Toast.LENGTH_SHORT).show()
            return
        }

        val user = auth.currentUser
        if (user != null) {
            val usuarioId = user.uid
            val torneoId = database.child("torneos").push().key
            if (torneoId != null) {
                val latitudString = torneoLatLng!!.latitude.toString()
                val longitudString = torneoLatLng!!.longitude.toString()

                val torneo = Torneo(
                    torneoId,
                    nombre,
                    localidad,
                    participantes,
                    fechaInicioInscripcion,
                    fechaFinInscripcion,
                    fechaComienzo,
                    fechaFin,
                    deporte,
                    usuarioId,
                    latitudString,
                    longitudString
                )

                Log.d("LatLng", "Latitud: $latitudString, Longitud: $longitudString")

                database.child("torneos").child(torneoId).setValue(torneo)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Torneo creado con éxito", Toast.LENGTH_SHORT).show()
                            finish()
                        } else {
                            Toast.makeText(this, "Error al guardar el torneo", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun validarFechasInscripcion(fechaInicio: String, fechaFin: String): Boolean {
        val fecha = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        try {
            val fechaInicioInscripcion = fecha.parse(fechaInicio)
            val fechaFinInscripcion = fecha.parse(fechaFin)
            // Validar que la fecha de fin de inscripción sea anterior a la fecha de inicio del torneo
            return !fechaInicioInscripcion.after(fechaFinInscripcion)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return false
    }
}

