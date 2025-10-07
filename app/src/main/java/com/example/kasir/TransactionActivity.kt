package com.example.kasir

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var rvJuiceMenu: RecyclerView
    private lateinit var rvOrderItems: RecyclerView
    private lateinit var juiceMenuAdapter: JuiceKasirAdapter
    private lateinit var orderItemAdapter: OrderItemAdapter
    private lateinit var tvTotal: TextView
    private lateinit var spinnerPaymentMethod: AutoCompleteTextView
    private lateinit var btnFinishTransaction: Button
    private lateinit var btnLogout: Button

    private val juiceMenuList = mutableListOf<JuiceItem>()
    private val currentOrder = mutableListOf<OrderItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        rvJuiceMenu = findViewById(R.id.rvJuiceMenu)
        rvOrderItems = findViewById(R.id.rvOrderItems)
        tvTotal = findViewById(R.id.tvTotal)
    spinnerPaymentMethod = findViewById(R.id.spinnerPaymentMethod)
        btnFinishTransaction = findViewById(R.id.btnFinishTransaction)
        btnLogout = findViewById(R.id.btnLogout)

        setupRecyclerViews()
        setupSpinner()
        fetchJuiceMenu()

        btnFinishTransaction.setOnClickListener {
            handleTransaction()
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun setupRecyclerViews() {
        rvJuiceMenu.layoutManager = LinearLayoutManager(this)
        juiceMenuAdapter = JuiceKasirAdapter(juiceMenuList) { juiceItem, quantity ->
            updateOrder(juiceItem, quantity)
        }
        rvJuiceMenu.adapter = juiceMenuAdapter

        rvOrderItems.layoutManager = LinearLayoutManager(this)
        orderItemAdapter = OrderItemAdapter(
            orderItems = currentOrder,
            onQuantityChanged = {
                updateTotal()
            },
            onItemRemoved = { removedItem ->
                juiceMenuAdapter.updateQuantity(removedItem.juiceItem, 0)
                updateTotal()
            }
        )
        rvOrderItems.adapter = orderItemAdapter
    }

    private fun setupSpinner() {
        val paymentMethods = arrayOf("Tunai", "QRIS")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, paymentMethods)
        spinnerPaymentMethod.setAdapter(adapter)
        spinnerPaymentMethod.setText(paymentMethods[0], false)
    }

    private fun fetchJuiceMenu() {
        db.collection("juice_menu").get()
            .addOnSuccessListener { result ->
                juiceMenuList.clear()
                for (document in result) {
                    val juiceItem = document.toObject(JuiceItem::class.java).copy(id = document.id)
                    juiceMenuList.add(juiceItem)
                }
                juiceMenuAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle error
            }
    }

    private fun updateOrder(juiceItem: JuiceItem, quantity: Int) {
        val existingOrderItem = currentOrder.find { it.juiceItem.id == juiceItem.id }

        if (existingOrderItem != null) {
            if (quantity > 0) {
                existingOrderItem.quantity = quantity
            } else {
                currentOrder.remove(existingOrderItem)
            }
        } else {
            if (quantity > 0) {
                currentOrder.add(OrderItem(juiceItem, quantity))
            }
        }
        orderItemAdapter.notifyDataSetChanged()
        updateTotal()
    }

    private fun updateTotal() {
        val total = currentOrder.sumOf { it.juiceItem.price * it.quantity }
        tvTotal.text = "Total: Rp${total.toInt()}"
    }

    private fun handleTransaction() {
        if (currentOrder.isEmpty()) {
            Toast.makeText(this, "Tidak ada item dalam pesanan", Toast.LENGTH_SHORT).show()
            return
        }

    val paymentMethod = spinnerPaymentMethod.text.toString()
        if (paymentMethod == "Tunai") {
            showCashInputDialog()
        } else {
            finishTransaction(0.0)
        }
    }

    private fun showCashInputDialog() {
        val totalAmount = currentOrder.sumOf { it.juiceItem.price * it.quantity }
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "Masukkan jumlah uang"

        AlertDialog.Builder(this)
            .setTitle("Pembayaran Tunai")
            .setMessage("Total Belanja: Rp${totalAmount.toInt()}")
            .setView(input)
            .setPositiveButton("Bayar") { _, _ ->
                val cashReceived = input.text.toString().toDoubleOrNull()
                if (cashReceived != null && cashReceived >= totalAmount) {
                    val change = cashReceived - totalAmount
                    finishTransaction(change)
                } else {
                    Toast.makeText(this, "Uang tidak cukup", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun finishTransaction(change: Double) {
        val totalAmount = currentOrder.sumOf { it.juiceItem.price * it.quantity }
    val paymentMethod = spinnerPaymentMethod.text.toString()

        val transaction = Transaction(
            timestamp = Date(),
            totalAmount = totalAmount,
            paymentMethod = paymentMethod,
            items = ArrayList(currentOrder)
        )

        db.collection("transactions").add(transaction)
            .addOnSuccessListener { documentReference ->
                showReceiptDialog(documentReference.id, transaction, change)
                currentOrder.clear()
                juiceMenuAdapter.resetQuantities()
                orderItemAdapter.notifyDataSetChanged()
                updateTotal()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menyimpan transaksi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showReceiptDialog(transactionId: String, transaction: Transaction, change: Double) {
        val receiptDetails = StringBuilder()
        receiptDetails.append("Struk Transaksi\n")
        receiptDetails.append("ID: $transactionId\n")
        transaction.timestamp?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            receiptDetails.append("Waktu: ${sdf.format(it)}\n\n")
        }
        transaction.items.forEach {
            receiptDetails.append("${it.juiceItem.name} x${it.quantity} - Rp${(it.juiceItem.price * it.quantity).toInt()}\n")
        }
        receiptDetails.append("\nTotal: Rp${transaction.totalAmount.toInt()}\n")
        receiptDetails.append("Metode Pembayaran: ${transaction.paymentMethod}\n")
        if (transaction.paymentMethod == "Tunai") {
            receiptDetails.append("Kembalian: Rp${change.toInt()}\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Transaksi Berhasil")
            .setMessage(receiptDetails.toString())
            .setPositiveButton("Tutup", null)
            .show()
    }
}
