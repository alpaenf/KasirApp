package com.example.kasir

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KasirProfileFragment : Fragment() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_profile, container, false)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Setup automatic theme
        setupTheme()
        
        // Setup profile information
        setupProfileInfo(view)
        
        // Setup logout button
        view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout)?.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = android.content.Intent(requireContext(), LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        
        return view
    }
    
    private fun setupTheme() {
        // Set theme to follow system automatically
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        // Check current theme
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        when (currentNightMode) {
            Configuration.UI_MODE_NIGHT_NO -> {
                // Light mode is active
            }
            Configuration.UI_MODE_NIGHT_YES -> {
                // Dark mode is active  
            }
        }
    }
    
    private fun setupProfileInfo(view: View) {
        val tvProfileName = view.findViewById<TextView>(R.id.tvProfileName)
        val tvProfileEmail = view.findViewById<TextView>(R.id.tvProfileEmail)
        
        // Get current user info
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            tvProfileEmail.text = user.email
            
            // Get username from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val username = document.getString("username") ?: "Kasir"
                        tvProfileName.text = username
                    }
                }
                .addOnFailureListener {
                    tvProfileName.text = "Kasir"
                }
        }
    }
}
