package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Memberi sedikit jeda untuk efek "splash screen"
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserStatus()
        }, 1500) // Jeda 1.5 detik
    }

    private fun checkUserStatus() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            // Pengguna sudah login, periksa role dan arahkan
            checkUserRoleAndRedirect(currentUser.uid)
        } else {
            // Tidak ada pengguna yang login, arahkan ke LoginActivity
            redirectToLogin()
        }
    }

    private fun checkUserRoleAndRedirect(userId: String) {
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    when (document.getString("role")) {
                        "Admin" -> navigateTo(AdminDashboardActivity::class.java)
                        "Kasir" -> navigateTo(KasirMainActivity::class.java)
                        else -> redirectToLogin() // Role tidak valid
                    }
                } else {
                    redirectToLogin() // Data user tidak ada di Firestore
                }
            }
            .addOnFailureListener {
                redirectToLogin() // Gagal mengambil data
            }
    }

    private fun redirectToLogin() {
        navigateTo(LoginActivity::class.java)
    }
    
    private fun <T : AppCompatActivity> navigateTo(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
        finish()
    }
}
