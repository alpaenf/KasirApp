package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.chip.ChipGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AdminDashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup Toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        val tvTotalPenjualan = view.findViewById<TextView>(R.id.tvTotalPenjualan)
        val tvJumlahProduk = view.findViewById<TextView>(R.id.tvJumlahProduk)
        val tvJumlahUser = view.findViewById<TextView?>(R.id.tvJumlahUser)
        val tvProdukTerlaris = view.findViewById<TextView?>(R.id.tvProdukTerlaris)
        val chipGroup = view.findViewById<ChipGroup>(R.id.chipGroupFilter)
        val barChart = view.findViewById<BarChart>(R.id.barChartPenjualan)

        val db = FirebaseFirestore.getInstance()

        fun updateStats(filter: String) {
            db.collection("transactions").get().addOnSuccessListener { snapshot ->
                var total = 0L
                val produkCount = mutableMapOf<String, Int>()
                val chartMap = mutableMapOf<String, Long>()
                val dateFormat = when (filter) {
                    "Hari" -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    "Bulan" -> SimpleDateFormat("MM/yyyy", Locale.getDefault())
                    else -> SimpleDateFormat("yyyy", Locale.getDefault())
                }
                val now = Calendar.getInstance()
                val todayStr = dateFormat.format(now.time)
                for (doc in snapshot.documents) {
                    val t = doc.getString("total")?.replace("[^\\d]".toRegex(), "") ?: "0"
                    val produk = doc.getString("produk") ?: "-"
                    val ts = doc.getTimestamp("timestamp")?.toDate() ?: continue
                    val key = dateFormat.format(ts)
                    // Filter by selected time
                    val match = when (filter) {
                        "Hari" -> key == todayStr
                        "Bulan" -> key == dateFormat.format(now.time)
                        else -> key == dateFormat.format(now.time)
                    }
                    if (match) {
                        total += t.toLongOrNull() ?: 0L
                        produkCount[produk] = produkCount.getOrDefault(produk, 0) + 1
                    }
                    chartMap[key] = (chartMap[key] ?: 0L) + (t.toLongOrNull() ?: 0L)
                }
                tvTotalPenjualan.text = "Rp ${formatRupiah(total)}"
                // Top produk
                val topProduk = produkCount.entries.sortedByDescending { it.value }.take(3)
                val topText = topProduk.joinToString("\n") { "${it.key} (${it.value}x)" }
                tvProdukTerlaris?.text = if (topText.isNotEmpty()) topText else "-"
                // BarChart
                val sortedKeys = chartMap.keys.sorted()
                val entries = sortedKeys.mapIndexed { idx, k -> BarEntry(idx.toFloat(), chartMap[k]?.toFloat() ?: 0f) }
                val dataSet = BarDataSet(entries, "Penjualan")
                dataSet.color = resources.getColor(R.color.primary_green, null)
                val barData = BarData(dataSet)
                barData.barWidth = 0.7f
                barChart.data = barData
                barChart.xAxis.valueFormatter = IndexAxisValueFormatter(sortedKeys)
                barChart.xAxis.granularity = 1f
                barChart.xAxis.labelRotationAngle = -45f
                barChart.axisLeft.axisMinimum = 0f
                barChart.axisRight.isEnabled = false
                barChart.description.isEnabled = false
                barChart.legend.isEnabled = false
                barChart.invalidate()
            }
        }

        // Initial: Hari
        updateStats("Hari")

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.chipHari -> "Hari"
                R.id.chipBulan -> "Bulan"
                R.id.chipTahun -> "Tahun"
                else -> "Hari"
            }
            updateStats(filter)
        }

        // Jumlah Produk
        db.collection("products").get().addOnSuccessListener { snapshot ->
            tvJumlahProduk.text = snapshot.size().toString()
        }

        // Jumlah User
        tvJumlahUser?.let {
            db.collection("users").get().addOnSuccessListener { snapshot ->
                it.text = snapshot.size().toString()
            }
        }
    }

    private fun formatRupiah(number: Long): String {
        return String.format("%,d", number).replace(',', '.')
    }
}
