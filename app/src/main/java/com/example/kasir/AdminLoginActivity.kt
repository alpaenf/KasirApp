package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLoginAdmin = findViewById<Button>(R.id.btnLoginAdmin)
        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        btnLoginAdmin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Email dan password tidak boleh kosong", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid
                        if (uid != null) {
                            db.collection("users").document(uid).get()
                                .addOnSuccessListener { document ->
                                    if (document != null && document.getString("role") == "admin") {
                                        Toast.makeText(baseContext, "Login admin berhasil.", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, AdminDashboardActivity::class.java))
                                        finishAffinity()
                                    } else {
                                        auth.signOut()
                                        Toast.makeText(baseContext, "Anda tidak memiliki akses admin.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                .addOnFailureListener { 
                                    auth.signOut()
                                    Toast.makeText(baseContext, "Gagal memverifikasi peran.", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(baseContext, "Gagal mendapatkan ID pengguna.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(baseContext, "Login gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvBackToLogin.setOnClickListener {
            finish() // Kembali ke halaman login sebelumnya
        }
    }
}