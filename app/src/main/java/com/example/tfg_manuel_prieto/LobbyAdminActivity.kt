package com.example.tfg_manuel_prieto

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LobbyAdminActivity : AppCompatActivity() {
    private lateinit var reportListView: ListView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lobby_admin)

        database = FirebaseDatabase.getInstance().reference

        reportListView = findViewById(R.id.reportListView)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        reportListView.adapter = adapter

        reportListView.visibility = View.GONE

        reportListView.setOnItemClickListener { _, _, position, _ ->
            val selectedReport = adapter.getItem(position)
            if (selectedReport != null) {
                mostrarDialogoAcciones(selectedReport)
            }
        }

        configurarFuncionalidadesAdmin()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun cargarReportes() {
        val reportesRef = database.child("reportes")

        // Leer los reportes de Firebase
        reportesRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val reportes = mutableListOf<String>()
                for (reportSnapshot in snapshot.children) {
                    val motivo = reportSnapshot.child("motivo").getValue(String::class.java) ?: ""
                    val mensaje = reportSnapshot.child("mensaje").getValue(String::class.java) ?: ""
                    reportes.add("$motivo - $mensaje")
                }
                actualizarListaReportes(reportes)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar reportes desde Firebase: ${error.message}")
            }
        })
    }

    private fun actualizarListaReportes(reportes: List<String>) {
        adapter.clear()
        adapter.addAll(reportes)
        adapter.notifyDataSetChanged()
    }

    private fun configurarFuncionalidadesAdmin() {
        findViewById<Button>(R.id.btnVerReportes).setOnClickListener {
            reportListView.visibility = View.VISIBLE
            cargarReportes()
        }
    }

    private fun mostrarDialogoAcciones(selectedReport: String) {
        val motivo = selectedReport.split(" - ")[0] // Obtener el motivo del reporte
        val mensaje = selectedReport.split(" - ")[1] // Obtener el mensaje reportado

        val dialog = AlertDialog.Builder(this)
            .setTitle("Acciones para el reporte")
            .setMessage("Seleccionaste el reporte:\nMotivo: $motivo\nMensaje: $mensaje")
            .setPositiveButton("Eliminar Mensajes") { _, _ ->
                eliminarMensajesReportados(motivo, mensaje)
            }
            .setNeutralButton("Cancelar", null)
            .create()

        dialog.show()
    }

    private fun eliminarMensajesReportados(motivo: String, mensaje: String) {
        val reportesRef = database.child("reportes")
        reportesRef.orderByChild("motivo").equalTo(motivo)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (reportSnapshot in snapshot.children) {
                            val reporteId = reportSnapshot.child("reporteId").getValue(String::class.java)
                            val idMensaje = reportSnapshot.child("messageId").getValue(String::class.java)

                            if (reporteId != null && idMensaje != null) {
                                eliminarMensaje(reporteId, idMensaje)
                                reportSnapshot.ref.removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(this@LobbyAdminActivity, "Mensaje reportado y eliminado correctamente", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this@LobbyAdminActivity, "Error al eliminar el mensaje reportado: ${exception.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this@LobbyAdminActivity, "No se encontró el reporte o el id del mensaje", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this@LobbyAdminActivity, "No se encontraron reportes para este motivo", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al buscar reporte para eliminar mensajes: ${error.message}")
                }
            })
    }

    private fun eliminarMensaje(reporteId: String, idMensaje: String) {
        val chatsRef = database.child("chats")

        chatsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var mensajeEncontrado = false

                for (torneoSnapshot in dataSnapshot.children) {
                    for (chatSnapshot in torneoSnapshot.children) {
                        val mensajeId = chatSnapshot.child("idMensaje").getValue(String::class.java)
                        if (mensajeId == idMensaje) {
                            chatSnapshot.ref.removeValue()
                                .addOnSuccessListener {
                                    mensajeEncontrado = true
                                    Log.d(TAG, "Mensaje eliminado correctamente del chat")
                                    Toast.makeText(this@LobbyAdminActivity, "Mensaje eliminado correctamente del chat", Toast.LENGTH_SHORT).show()
                                }
                                .addOnFailureListener { exception ->
                                    Log.e(TAG, "Error al eliminar el mensaje del chat: ${exception.message}")
                                    Toast.makeText(this@LobbyAdminActivity, "Error al eliminar el mensaje del chat: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            break
                        }
                    }
                    if (mensajeEncontrado) {
                        Log.e(TAG, "No se encontró el mensaje correspondiente para eliminar")
                        Toast.makeText(this@LobbyAdminActivity, "No se encontró el mensaje correspondiente para eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(TAG, "Error al cargar mensaje para eliminar: ${databaseError.message}")
                Toast.makeText(this@LobbyAdminActivity, "Error al cargar mensaje para eliminar: ${databaseError.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    companion object {
        private const val TAG = "LobbyAdminActivity"
    }
}