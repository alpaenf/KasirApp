package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KasirDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupUI()
        loadUserInfo()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar_kasir_dashboard)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Dashboard Kasir"
    }

    private fun setupUI() {
        val btnGoToTransaction = findViewById<Button>(R.id.btnGoToTransaction)
        val btnGoToHistory = findViewById<Button>(R.id.btnGoToHistory)
        val btnGoToReport = findViewById<Button>(R.id.btnGoToReport)

        btnGoToTransaction.setOnClickListener {
            // Buka activity untuk transaksi/kasir dengan bottom navigation
            startActivity(Intent(this, KasirMainActivity::class.java))
        }

        btnGoToHistory.setOnClickListener {
            // Buka activity untuk riwayat transaksi
            startActivity(Intent(this, KasirMainActivity::class.java))
        }

        btnGoToReport.setOnClickListener {
            // Buka activity untuk laporan
            startActivity(Intent(this, KasirMainActivity::class.java))
        }
    }

    private fun loadUserInfo() {
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val currentUser = auth.currentUser
        
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username") ?: "Kasir"
                        tvWelcome.text = "Selamat Datang, $username!"
                    }
                }
                .addOnFailureListener {
                    tvWelcome.text = "Selamat Datang, Kasir!"
                }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_kasir_dashboard, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
