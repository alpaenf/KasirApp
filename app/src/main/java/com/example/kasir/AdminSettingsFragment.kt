package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AdminSettingsFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        val btnEditSettings = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditSettings)
        btnEditSettings.setOnClickListener {
            com.google.android.material.snackbar.Snackbar.make(view, "Edit pengaturan belum diimplementasi", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            // TODO: Ganti dengan intent ke halaman edit pengaturan
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_settings, container, false)
    }
}
