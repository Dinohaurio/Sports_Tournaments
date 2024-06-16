package com.example.tfg_manuel_prieto

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegistroActivity : AppCompatActivity() {
    private lateinit var txtName: EditText
    private lateinit var txtEmail: EditText
    private lateinit var txtPassword: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var chkPrivacyPolicy: CheckBox
    private lateinit var txtPrivacyPolicyLink: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        initialise()
        setupPrivacyPolicyLink()
    }

    private fun initialise() {
        txtName = findViewById(R.id.txtName)
        txtEmail = findViewById(R.id.txtEmail)
        txtPassword = findViewById(R.id.txtPassword)
        progressBar = findViewById(R.id.progressBar)
        chkPrivacyPolicy = findViewById(R.id.chkPrivacyPolicy)
        txtPrivacyPolicyLink = findViewById(R.id.txtPrivacyPolicyLink)
        progressBar.visibility = View.GONE
        database = FirebaseDatabase.getInstance()
        auth = FirebaseAuth.getInstance()
        databaseReference = database.reference.child("Users")
    }

    private fun setupPrivacyPolicyLink() {
        val spannableString = SpannableString(getString(R.string.chk_pol_priv))
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                mostrarPoliticaPrivacidadDialog()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(this@RegistroActivity, R.color.white)
                ds.isUnderlineText = true
            }
        }
        spannableString.setSpan(
            clickableSpan,
            0,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        txtPrivacyPolicyLink.text = spannableString
        txtPrivacyPolicyLink.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun mostrarPoliticaPrivacidadDialog() {
        AlertDialog.Builder(this)
            .setTitle("Política de Privacidad")
            .setMessage(getString(R.string.politica_privacidad))
            .setPositiveButton(android.R.string.ok, null)
            .create()
            .show()
    }

    private fun crearCuenta() {
        val name = txtName.text.toString().trim()
        val email = txtEmail.text.toString().trim()
        val password = txtPassword.text.toString().trim()

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(name) && chkPrivacyPolicy.isChecked) {
            if (emailValido(email) && passwordValido(password)) {
                progressBar.visibility = View.VISIBLE

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user: FirebaseUser = auth.currentUser!!
                            guardarusuario(user.uid, name, email)
                            verificarEmail(user)
                            updateUserInfoAndGoHome()
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(
                                this,
                                "Error en la autenticación: ${task.exception?.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Correo electrónico o contraseña no válidos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            if (!chkPrivacyPolicy.isChecked) {
                Toast.makeText(this, "Debe aceptar la Política de Privacidad", Toast.LENGTH_SHORT)
                    .show()
            } else {
                Toast.makeText(this, "Llene todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun registrar(view: View) {
        crearCuenta()
    }

    private fun guardarusuario(userId: String, name: String, email: String) {
        val currentUserDb = databaseReference.child(userId)
        currentUserDb.child("nombre").setValue(name)
        currentUserDb.child("email").setValue(email)
    }

    private fun updateUserInfoAndGoHome() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        progressBar.visibility = View.GONE
        finish()
    }

    private fun verificarEmail(user: FirebaseUser) {
        user.sendEmailVerification()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Email de verificación enviado a ${user.email}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Error al verificar el correo: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun emailValido(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun passwordValido(password: String): Boolean {
        return password.length >= 6
    }
}