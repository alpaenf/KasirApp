package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KasirReportFragment : Fragment() {
    
    private lateinit var db: FirebaseFirestore
    private lateinit var tvTotalOmzetLaporan: TextView
    private lateinit var tvTotalTransaksiLaporan: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_report, container, false)
        
        // Initialize Firebase
        db = FirebaseFirestore.getInstance()
        
        // Setup automatic theme
        setupTheme()
        
        // Initialize views
        initViews(view)
        
        // Load daily report
        loadDailyReport()
        
        return view
    }
    
    private fun setupTheme() {
        // Set theme to follow system automatically
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
    
    private fun initViews(view: View) {
        try {
            tvTotalOmzetLaporan = view.findViewById(R.id.tvTotalOmzetLaporan)
            tvTotalTransaksiLaporan = view.findViewById(R.id.tvTotalTransaksiLaporan)
        } catch (e: Exception) {
            // Handle view initialization error
            e.printStackTrace()
        }
    }
    
    private fun loadDailyReport() {
        try {
            val today = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayString = dateFormat.format(today.time)
            
            db.collection("transactions")
                .whereEqualTo("date", todayString)
                .get()
                .addOnSuccessListener { result ->
                    try {
                        var totalSales = 0.0
                        var totalTransactions = 0
                        
                        for (document in result) {
                            totalTransactions++
                            
                            // Get items array and calculate total
                            val items = document.get("items") as? List<Map<String, Any>>
                            items?.forEach { item ->
                                val price = when (val priceValue = item["price"]) {
                                    is Double -> priceValue
                                    is Long -> priceValue.toDouble()
                                    is String -> priceValue.toDoubleOrNull() ?: 0.0
                                    else -> 0.0
                                }
                                
                                val quantity = when (val qtyValue = item["quantity"]) {
                                    is Int -> qtyValue
                                    is Long -> qtyValue.toInt()
                                    is Double -> qtyValue.toInt()
                                    is String -> qtyValue.toIntOrNull() ?: 1
                                    else -> 1
                                }
                                
                                totalSales += price * quantity
                            }
                        }
                        
                        // Update UI safely
                        activity?.runOnUiThread {
                            if (::tvTotalOmzetLaporan.isInitialized && ::tvTotalTransaksiLaporan.isInitialized) {
                                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                                tvTotalOmzetLaporan.text = formatter.format(totalSales)
                                tvTotalTransaksiLaporan.text = "$totalTransactions"
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        setDefaultValues()
                    }
                }
                .addOnFailureListener { exception ->
                    exception.printStackTrace()
                    setDefaultValues()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            setDefaultValues()
        }
    }
    
    private fun setDefaultValues() {
        activity?.runOnUiThread {
            if (::tvTotalOmzetLaporan.isInitialized && ::tvTotalTransaksiLaporan.isInitialized) {
                tvTotalOmzetLaporan.text = "Rp 0"
                tvTotalTransaksiLaporan.text = "0"
            }
        }
    }
}