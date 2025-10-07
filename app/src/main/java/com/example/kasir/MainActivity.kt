package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var rvJuiceMenu: RecyclerView
    private lateinit var tvTotalAmount: TextView
    private lateinit var btnPayCash: Button
    private lateinit var btnPayQRIS: Button
    private lateinit var btnTransactionHistory: Button
    private lateinit var juiceKasirAdapter: JuiceKasirAdapter
    private lateinit var auth: FirebaseAuth

    private var currentOrderItems = mutableListOf<OrderItem>()
    private var totalAmount = 0.0

    private val transactionHistory = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)
        rvJuiceMenu = findViewById(R.id.rvJuiceMenu)
        tvTotalAmount = findViewById(R.id.tvTotalAmount)
        btnPayCash = findViewById(R.id.btnPayCash)
        btnPayQRIS = findViewById(R.id.btnPayQRIS)
        btnTransactionHistory = findViewById(R.id.btnTransactionHistory)
        auth = FirebaseAuth.getInstance()

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        ViewCompat.setOnApplyWindowInsetsListener(drawerLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupRecyclerView()
        updateTotalAmountUI()

        btnPayCash.setOnClickListener {
            if (currentOrderItems.isEmpty()) {
                Toast.makeText(this, "Tidak ada item yang dipesan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showCashPaymentDialog()
        }

        btnPayQRIS.setOnClickListener {
            if (currentOrderItems.isEmpty()) {
                Toast.makeText(this, "Tidak ada item yang dipesan", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showQRISPaymentDialog()
        }

        btnTransactionHistory.setOnClickListener { 
            if (transactionHistory.isEmpty()) {
                Toast.makeText(this, "Belum ada riwayat transaksi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = TransactionHistoryActivity.newIntent(this, ArrayList(transactionHistory))
            startActivity(intent)
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                auth.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun getDummyJuiceMenu(): List<JuiceItem> {
        return listOf(
            JuiceItem("1", "Jus Alpukat", 15000.0),
            JuiceItem("2", "Jus Mangga", 12000.0),
            JuiceItem("3", "Jus Jeruk", 10000.0),
            JuiceItem("4", "Jus Tomat", 8000.0),
            JuiceItem("5", "Jus Wortel", 8000.0)
        )
    }

    private fun setupRecyclerView() {
        val juiceList = getDummyJuiceMenu()
        juiceKasirAdapter = JuiceKasirAdapter(juiceList) { juiceItem, quantity ->
            val existingItem = currentOrderItems.find { it.juiceItem.id == juiceItem.id }
            if (quantity > 0) {
                if (existingItem != null) {
                    existingItem.quantity = quantity
                } else {
                    currentOrderItems.add(OrderItem(juiceItem, quantity))
                }
            } else {
                currentOrderItems.removeAll { it.juiceItem.id == juiceItem.id }
            }
            calculateTotalAmount()
            updateTotalAmountUI()
        }
        rvJuiceMenu.adapter = juiceKasirAdapter
        rvJuiceMenu.layoutManager = LinearLayoutManager(this)
    }

    private fun calculateTotalAmount() {
        totalAmount = currentOrderItems.sumOf { it.juiceItem.price * it.quantity }
    }

    private fun updateTotalAmountUI() {
        tvTotalAmount.text = "${totalAmount.toInt()}"
    }

    private fun showCashPaymentDialog() {
    val editTextAmount = EditText(this)
    editTextAmount.inputType = android.text.InputType.TYPE_CLASS_NUMBER
    editTextAmount.hint = "Jumlah uang diterima"

        AlertDialog.Builder(this)
            .setTitle("Pembayaran Tunai")
            .setMessage("Total Belanja: Rp${totalAmount.toInt()}")
            .setView(editTextAmount)
            .setPositiveButton("Bayar") { dialog, _ ->
                val amountPaidString = editTextAmount.text.toString()
                if (amountPaidString.isNotEmpty()) {
                    val amountPaid = amountPaidString.toDouble()
                    if (amountPaid >= totalAmount) {
                        val change = amountPaid - totalAmount
                        processTransaction("Cash")
                        dialog.dismiss()
                    } else {
                        Toast.makeText(this, "Uang tidak cukup", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Masukkan jumlah uang", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun showQRISPaymentDialog() {
        AlertDialog.Builder(this)
            .setTitle("Pembayaran QRIS")
            .setMessage("Silakan pindai QR Code berikut (simulasi).\nTotal Belanja: Rp${totalAmount.toInt()}")
            .setPositiveButton("Konfirmasi Bayar") { dialog, _ ->
                processTransaction("QRIS")
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun processTransaction(paymentMethod: String) {
        val transactionId = UUID.randomUUID().toString()
        val timestamp = Date()

        val newTransaction = Transaction(
            id = transactionId,
            items = ArrayList(currentOrderItems), 
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            timestamp = timestamp
        )
        transactionHistory.add(newTransaction)

        showReceipt(newTransaction)
        resetOrder()
    }

    private fun showReceipt(transaction: Transaction) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val transactionTime = transaction.timestamp?.let { dateFormat.format(it) } ?: "N/A"

        val itemsDetails = transaction.items.joinToString("\n") {
            "- ${it.juiceItem.name} x${it.quantity} (@ Rp${it.juiceItem.price.toInt()}) = Rp${(it.juiceItem.price * it.quantity).toInt()}"
        }

        var receiptMessage = "Struk Pembayaran\n"
        receiptMessage += "---------------------\n"
        receiptMessage += "ID Transaksi: ${transaction.id.substring(0, 8)}\n"
        receiptMessage += "Waktu: $transactionTime\n"
        receiptMessage += "---------------------\n"
        receiptMessage += "Pesanan:\n$itemsDetails\n"
        receiptMessage += "---------------------\n"
        receiptMessage += "Total: Rp${transaction.totalAmount.toInt()}\n"
        receiptMessage += "Metode: ${transaction.paymentMethod}\n"
        if (transaction.paymentMethod == "Cash") {
            //  receiptMessage += "Dibayar: Rp${transaction.amountPaid?.toInt()}\n"
            // receiptMessage += "Kembalian: Rp${transaction.change?.toInt()}\n"
        }
        receiptMessage += "---------------------\n"
        receiptMessage += "Terima Kasih!"

        AlertDialog.Builder(this)
            .setTitle("Transaksi Berhasil")
            .setMessage(receiptMessage)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun resetOrder() {
        currentOrderItems.clear()
        totalAmount = 0.0
        updateTotalAmountUI()
        juiceKasirAdapter.resetQuantities()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_logout -> {
                showLogoutConfirmationDialog()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
