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

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvAdminLogin = findViewById<TextView>(R.id.tvAdminLogin)

        btnLogin.setOnClickListener {
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
                                    if (document != null && document.getString("role") == "kasir") {
                                        Toast.makeText(baseContext, "Login kasir berhasil.", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, KasirMainActivity::class.java))
                                        finishAffinity()
                                    } else {
                                        auth.signOut()
                                        Toast.makeText(baseContext, "Hanya kasir yang bisa login di sini.", Toast.LENGTH_SHORT).show()
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

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvAdminLogin.setOnClickListener {
            startActivity(Intent(this, AdminLoginActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")
                        if (role == "kasir") {
                            startActivity(Intent(this, KasirMainActivity::class.java))
                            finish()
                        } else if (role == "admin") {
                            // Optional: Redirect admin to their dashboard if they open the app again
                            // startActivity(Intent(this, AdminDashboardActivity::class.java))
                            // finish()
                        }
                    }
            }
        }
    }
}
