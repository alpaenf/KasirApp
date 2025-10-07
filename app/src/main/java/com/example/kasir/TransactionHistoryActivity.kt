package com.example.kasir

import android.content.Context
import android.content.Intent
import android.os.Build // Import ditambahkan
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.ArrayList

class TransactionHistoryActivity : AppCompatActivity() {

    private lateinit var rvTransactionHistory: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter // Di-uncomment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        rvTransactionHistory = findViewById(R.id.rvTransactionHistory)
        rvTransactionHistory.layoutManager = LinearLayoutManager(this)

        // Mengambil data transaksi dari intent
        // Tipe eksplisit ditambahkan untuk kejelasan
        val transactions: ArrayList<Transaction>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra(EXTRA_TRANSACTIONS, Transaction::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayListExtra(EXTRA_TRANSACTIONS)
        }

        // Memeriksa null dan apakah list tidak kosong sebelum membuat adapter
        if (transactions != null && transactions.isNotEmpty()) {
            transactionAdapter = TransactionAdapter(transactions) // Di-uncomment dan diinisialisasi
            rvTransactionHistory.adapter = transactionAdapter      // Di-uncomment dan di-set
        } else {
            // Opsional: Tampilkan pesan jika tidak ada riwayat transaksi
            // Misalnya, menggunakan TextView yang tersembunyi/terlihat atau Toast
            // Untuk saat ini, jika kosong, RecyclerView akan tampil kosong.
        }
    }

    companion object {
        private const val EXTRA_TRANSACTIONS = "extra_transactions"

        fun newIntent(context: Context, transactions: ArrayList<Transaction>): Intent {
            val intent = Intent(context, TransactionHistoryActivity::class.java)
            intent.putParcelableArrayListExtra(EXTRA_TRANSACTIONS, transactions)
            return intent
        }
    }
}