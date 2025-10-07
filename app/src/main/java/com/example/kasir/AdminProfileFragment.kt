package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class AdminProfileFragment : Fragment() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        val btnEditProfile = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnEditProfile)
        btnEditProfile.setOnClickListener {
            com.google.android.material.snackbar.Snackbar.make(view, "Edit profil belum diimplementasi", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
            // TODO: Ganti dengan intent ke halaman edit profil
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_profile, container, false)
    }
}
