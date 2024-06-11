package com.example.tfg_manuel_prieto

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    private val TAG = "LoginActivity"
    private var email by Delegates.notNull<String>()
    private var password by Delegates.notNull<String>()
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var mProgressBar: ProgressDialog

    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar componentes de la UI primero
        inicializar()

        mAuth = FirebaseAuth.getInstance()

        // Verificar si hay un usuario autenticado
        val currentUser = mAuth.currentUser
        if (currentUser != null) {
            irHome()
        }
    }

    private fun inicializar() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        mProgressBar = ProgressDialog(this)
    }

    private fun iniciarSesion() {
        email = etEmail.text.toString()
        password = etPassword.text.toString()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mProgressBar.setMessage("Iniciando sesión...")
            mProgressBar.show()

            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        irHome()
                    } else {
                        Toast.makeText(this, "Autenticación fallida.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Ingrese todos los detalles", Toast.LENGTH_SHORT).show()
        }
    }

    private fun irHome() {
        mProgressBar.hide()
        val intent = Intent(this, LobbyActivity::class.java)
        startActivity(intent)
        finish() // Finaliza MainActivity para que no se pueda volver atrás a ella
    }

    fun iniciarSesion(view: View) {
        iniciarSesion()
    }

    fun olvidoPassword(view: View) {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    fun registrar(view: View) {
        startActivity(Intent(this, RegistroActivity::class.java))
    }
}