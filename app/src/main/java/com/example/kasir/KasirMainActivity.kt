package com.example.kasir

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class KasirMainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set automatic theme based on system setting
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir_main)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setupToolbar()
        setupNavigationDrawer()
        
        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(KasirHomeFragment())
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setupNavigationDrawer() {
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, findViewById<Toolbar>(R.id.toolbar),
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)

        // Setup header info
        setupNavigationHeader()
    }

    private fun setupNavigationHeader() {
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navigationView.getHeaderView(0)
        val navHeaderName = headerView.findViewById<TextView>(R.id.nav_header_name)
        val navHeaderEmail = headerView.findViewById<TextView>(R.id.nav_header_email)
        
        // Get current user info
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            navHeaderEmail.text = user.email
            
            // Get username from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val username = document.getString("username")
                        navHeaderName.text = username ?: "Kasir"
                    }
                }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.kasir_nav_host_fragment, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                loadFragment(KasirHomeFragment())
            }
            R.id.nav_history -> {
                loadFragment(TransactionHistoryFragment())
            }
            R.id.nav_profile -> {
                loadFragment(KasirProfileFragment())
            }
            R.id.nav_logout -> {
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finishAffinity()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
