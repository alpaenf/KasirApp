package com.example.kasir

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class KasirHomeFragment : Fragment() {
    
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var tvWelcome: TextView
    private lateinit var tvTodayTotal: TextView
    private lateinit var btnTransaksiBaru: Button

    private lateinit var btnLaporanHarian: Button
    private lateinit var btnLihatMenu: Button
    private lateinit var btnKalkulator: Button
    private lateinit var btnPengaturan: Button
    private lateinit var tvTodayTransactions: TextView
    private lateinit var tvAvgTransaction: TextView
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_home, container, false)
        
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        // Initialize views
        initViews(view)
        
        // Set up click listeners
        setupClickListeners()
        
        // Setup automatic theme
        setupTheme()
        
        // Load today's sales data
        loadTodaySales()
        
        // Update welcome message
        updateWelcomeMessage()
        
        return view
    }
    
    private fun initViews(view: View) {
        tvWelcome = view.findViewById(R.id.tvWelcome)
        tvTodayTotal = view.findViewById(R.id.tvTodayTotal)
        btnTransaksiBaru = view.findViewById(R.id.btnTransaksiBaru)

        tvTodayTransactions = view.findViewById(R.id.tvTodayTransactions)
        tvAvgTransaction = view.findViewById(R.id.tvAvgTransaction)
        
        // Add more buttons
        btnLaporanHarian = view.findViewById(R.id.btnLaporanHarian)
        btnLihatMenu = view.findViewById(R.id.btnLihatMenu)
        btnKalkulator = view.findViewById(R.id.btnKalkulator)
        btnPengaturan = view.findViewById(R.id.btnPengaturan)
    }
    
    private fun setupClickListeners() {
        
                

        btnTransaksiBaru.setOnClickListener {
            startActivity(Intent(requireContext(), TransactionActivity::class.java))
        }
        
        btnLaporanHarian.setOnClickListener {
            showDailyReportDialog()
        }
        
        btnLihatMenu.setOnClickListener {
            showMenuDialog()
        }
        
        btnKalkulator.setOnClickListener {
            showCalculatorDialog()
        }
        
        btnPengaturan.setOnClickListener {
            showSettingsDialog()
        }
    }
    
    private fun loadTodaySales() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val tomorrow = Calendar.getInstance()
        tomorrow.time = today.time
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", today.time)
            .whereLessThan("timestamp", tomorrow.time)
            .get()
            .addOnSuccessListener { result ->
                var totalSales = 0.0
                var transactionCount = 0
                
                for (document in result) {
                    val amount = document.getDouble("totalAmount") ?: 0.0
                    totalSales += amount
                    transactionCount++
                }
                
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                tvTodayTotal.text = formatter.format(totalSales)
                tvTodayTransactions.text = transactionCount.toString()
                
                val avgTransaction = if (transactionCount > 0) totalSales / transactionCount else 0.0
                tvAvgTransaction.text = formatter.format(avgTransaction)
            }
            .addOnFailureListener { exception ->
                tvTodayTotal.text = "Rp 0"
                tvTodayTransactions.text = "0"
                tvAvgTransaction.text = "Rp 0"
            }
    }
    
    private fun updateWelcomeMessage() {
        val currentUser = auth.currentUser
        val userName = currentUser?.displayName ?: currentUser?.email ?: "Kasir"
        tvWelcome.text = "Selamat Datang, $userName!"
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this fragment
        loadTodaySales()
    }
    
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
    
    private fun showDailyReportDialog() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val tomorrow = Calendar.getInstance()
        tomorrow.time = today.time
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", today.time)
            .whereLessThan("timestamp", tomorrow.time)
            .get()
            .addOnSuccessListener { result ->
                val reportBuilder = StringBuilder()
                reportBuilder.append("LAPORAN HARIAN\n")
                reportBuilder.append("Tanggal: ${SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())}\n\n")
                
                var totalSales = 0.0
                var transactionCount = 0
                val productSales = mutableMapOf<String, Int>()
                
                for (document in result) {
                    val amount = document.getDouble("totalAmount") ?: 0.0
                    totalSales += amount
                    transactionCount++
                    
                    // Count product sales
                    val items = document.get("items") as? List<Map<String, Any>>
                    items?.forEach { item ->
                        val juiceItem = item["juiceItem"] as? Map<String, Any>
                        val name = juiceItem?.get("name")?.toString() ?: "Unknown"
                        val quantity = (item["quantity"] as? Long)?.toInt() ?: 0
                        productSales[name] = productSales.getOrDefault(name, 0) + quantity
                    }
                }
                
                val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                reportBuilder.append("Total Transaksi: $transactionCount\n")
                reportBuilder.append("Total Penjualan: ${formatter.format(totalSales)}\n")
                if (transactionCount > 0) {
                    reportBuilder.append("Rata-rata per Transaksi: ${formatter.format(totalSales / transactionCount)}\n")
                }
                
                reportBuilder.append("\nPRODUK TERLARIS:\n")
                productSales.entries.sortedByDescending { it.value }.take(5).forEach { (product, qty) ->
                    reportBuilder.append("• $product: $qty unit\n")
                }
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Laporan Harian")
                    .setMessage(reportBuilder.toString())
                    .setPositiveButton("Tutup", null)
                    .show()
            }
            .addOnFailureListener {
                showToast("Gagal memuat laporan harian")
            }
    }
    
    private fun showCalculatorDialog() {
        val calculatorView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_calculator, null)
        
        val etDisplay = calculatorView.findViewById<EditText>(R.id.etDisplay)
        var currentNumber = ""
        var operator = ""
        var previousNumber = ""
        
        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnPlus, R.id.btnMinus, R.id.btnMultiply, R.id.btnDivide,
            R.id.btnEquals, R.id.btnClear, R.id.btnDot, R.id.btnBackspace
        )
        
        buttons.forEach { buttonId ->
            calculatorView.findViewById<Button>(buttonId)?.setOnClickListener { button ->
                val buttonText = (button as Button).text.toString()
                
                when (buttonText) {
                    "C" -> {
                        currentNumber = ""
                        operator = ""
                        previousNumber = ""
                        etDisplay.setText("0")
                    }
                    "⌫" -> {
                        if (currentNumber.isNotEmpty()) {
                            currentNumber = currentNumber.dropLast(1)
                            etDisplay.setText(if (currentNumber.isEmpty()) "0" else currentNumber)
                        }
                    }
                    "+", "-", "×", "÷" -> {
                        if (currentNumber.isNotEmpty()) {
                            previousNumber = currentNumber
                            operator = buttonText
                            currentNumber = ""
                        }
                    }
                    "=" -> {
                        if (currentNumber.isNotEmpty() && previousNumber.isNotEmpty() && operator.isNotEmpty()) {
                            val prev = previousNumber.toDoubleOrNull() ?: 0.0
                            val curr = currentNumber.toDoubleOrNull() ?: 0.0
                            val result = when (operator) {
                                "+" -> prev + curr
                                "-" -> prev - curr
                                "×" -> prev * curr
                                "÷" -> if (curr != 0.0) prev / curr else 0.0
                                else -> 0.0
                            }
                            val resultText = if (result == result.toLong().toDouble()) {
                                result.toLong().toString()
                            } else {
                                String.format("%.2f", result)
                            }
                            etDisplay.setText(resultText)
                            currentNumber = resultText
                            operator = ""
                            previousNumber = ""
                        }
                    }
                    "." -> {
                        if (!currentNumber.contains(".")) {
                            currentNumber += if (currentNumber.isEmpty()) "0." else "."
                            etDisplay.setText(currentNumber)
                        }
                    }
                    else -> {
                        if (currentNumber == "0") {
                            currentNumber = buttonText
                        } else {
                            currentNumber += buttonText
                        }
                        etDisplay.setText(currentNumber)
                    }
                }
            }
        }
        
        AlertDialog.Builder(requireContext())
            .setTitle("Kalkulator")
            .setView(calculatorView)
            .setNegativeButton("Tutup", null)
            .show()
    }
    
    private fun showSettingsDialog() {
        val options = arrayOf(
            "Logout",
            "Backup Data",
            "Reset Hari Ini",
            "Tentang Aplikasi"
        )
        
        AlertDialog.Builder(requireContext())
            .setTitle("Pengaturan Kasir")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showLogoutConfirmation()
                    1 -> performBackupData()
                    2 -> showResetConfirmation()
                    3 -> showAboutDialog()
                }
            }
            .show()
    }
    
    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                auth.signOut()
                startActivity(Intent(requireContext(), LoginActivity::class.java))
                requireActivity().finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
    
    private fun performBackupData() {
        showToast("Memulai backup data...")
        
        db.collection("transactions")
            .get()
            .addOnSuccessListener { result ->
                val backupData = mutableMapOf<String, Any>()
                backupData["backup_timestamp"] = Date()
                backupData["total_transactions"] = result.size()
                
                val transactions = mutableListOf<Map<String, Any>>()
                for (document in result) {
                    val transactionData = document.data.toMutableMap()
                    transactionData["id"] = document.id
                    transactions.add(transactionData)
                }
                backupData["transactions"] = transactions
                
                // Simpan backup ke collection terpisah
                db.collection("backups")
                    .add(backupData)
                    .addOnSuccessListener { documentReference ->
                        showToast("Backup berhasil! ID: ${documentReference.id.take(8)}")
                    }
                    .addOnFailureListener { e ->
                        showToast("Backup gagal: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showToast("Gagal mengambil data untuk backup: ${e.message}")
            }
    }
    
    private fun showResetConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Reset Data Hari Ini")
            .setMessage("PERINGATAN: Ini akan menghapus SEMUA transaksi hari ini secara permanen!\n\nApakah Anda yakin ingin melanjutkan?")
            .setPositiveButton("Ya, Reset") { _, _ ->
                performResetTodayData()
            }
            .setNegativeButton("Batal", null)
            .show()
    }
    
    private fun performResetTodayData() {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val tomorrow = Calendar.getInstance()
        tomorrow.time = today.time
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        
        showToast("Menghapus transaksi hari ini...")
        
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", today.time)
            .whereLessThan("timestamp", tomorrow.time)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    showToast("Tidak ada transaksi hari ini untuk dihapus")
                    return@addOnSuccessListener
                }
                
                val batch = db.batch()
                for (document in result) {
                    batch.delete(document.reference)
                }
                
                batch.commit()
                    .addOnSuccessListener {
                        showToast("Berhasil menghapus ${result.size()} transaksi hari ini")
                        // Refresh data dashboard
                        loadTodaySales()
                    }
                    .addOnFailureListener { e ->
                        showToast("Gagal menghapus transaksi: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                showToast("Gagal mengambil data transaksi: ${e.message}")
            }
    }
    
    private fun showAboutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Tentang Aplikasi Kasir")
            .setMessage("""
                Kasir App v1.0
                
                Aplikasi kasir sederhana untuk mengelola transaksi penjualan.
                
                Fitur:
                • Transaksi penjualan
                • Riwayat transaksi
                • Laporan harian
                • Kalkulator built-in
                • Integrasi Firebase
                
                Dibuat dengan ❤️ untuk kemudahan bisnis Anda.
            """.trimIndent())
            .setPositiveButton("Tutup", null)
            .show()
    }
    
    private fun showMenuDialog() {
        db.collection("juice_menu")
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    showToast("Menu masih kosong")
                    return@addOnSuccessListener
                }
                
                val menuBuilder = StringBuilder()
                menuBuilder.append("DAFTAR MENU JUS\n\n")
                
                for (document in result) {
                    val juice = document.toObject(JuiceItem::class.java)
                    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    menuBuilder.append("• ${juice.name}\n")
                    menuBuilder.append("  ${formatter.format(juice.price)}\n\n")
                }
                
                AlertDialog.Builder(requireContext())
                    .setTitle("Menu Tersedia")
                    .setMessage(menuBuilder.toString())
                    .setPositiveButton("Tutup", null)
                    .show()
            }
            .addOnFailureListener {
                showToast("Gagal memuat menu")
            }
    }
    
    private fun setupTheme() {
        // Set theme to follow system automatically
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        
        // Check current theme and update UI accordingly
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
}
