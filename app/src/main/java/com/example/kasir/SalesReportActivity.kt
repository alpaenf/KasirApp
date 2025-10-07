package com.example.kasir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class SalesReportActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvTransactions: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactionList = mutableListOf<Transaction>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sales_report)

        db = FirebaseFirestore.getInstance()

        rvTransactions = findViewById(R.id.rvTransactions)
        rvTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(transactionList)
        rvTransactions.adapter = transactionAdapter

        fetchTransactions()
    }

    private fun fetchTransactions() {
        db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                transactionList.clear()
                if (snapshots != null) {
                    for (document in snapshots) {
                        val transaction = document.toObject(Transaction::class.java).copy(id = document.id)
                        transactionList.add(transaction)
                    }
                }
                transactionAdapter.notifyDataSetChanged()
            }
    }
}