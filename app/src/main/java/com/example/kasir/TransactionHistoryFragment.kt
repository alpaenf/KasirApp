package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class TransactionHistoryFragment : Fragment() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private val transactions = mutableListOf<Transaction>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_transaction_history, container, false)
        
        recyclerView = view.findViewById(R.id.recyclerViewTransactions)
        setupRecyclerView()
        loadTransactions()
        
        return view
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(transactions)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = transactionAdapter
    }

    private fun loadTransactions() {
        db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                transactions.clear()
                for (document in documents) {
                    try {
                        // Parse items correctly
                        val itemsList = mutableListOf<OrderItem>()
                        val items = document.get("items") as? List<Map<String, Any>>
                        items?.forEach { itemMap ->
                            val juiceItemMap = itemMap["juiceItem"] as? Map<String, Any>
                            if (juiceItemMap != null) {
                                val juiceItem = JuiceItem(
                                    id = juiceItemMap["id"]?.toString() ?: "",
                                    name = juiceItemMap["name"]?.toString() ?: "Unknown",
                                    price = (juiceItemMap["price"] as? Number)?.toDouble() ?: 0.0
                                )
                                val quantity = (itemMap["quantity"] as? Number)?.toInt() ?: 1
                                itemsList.add(OrderItem(juiceItem, quantity))
                            }
                        }
                        
                        val transaction = Transaction(
                            id = document.id,
                            timestamp = document.getTimestamp("timestamp")?.toDate(),
                            totalAmount = document.getDouble("totalAmount") ?: 0.0,
                            paymentMethod = document.getString("paymentMethod") ?: "Cash",
                            items = itemsList
                        )
                        transactions.add(transaction)
                    } catch (e: Exception) {
                        // Try alternative structure
                        try {
                            val transaction = Transaction(
                                id = document.id,
                                timestamp = document.getTimestamp("timestamp")?.toDate(),
                                totalAmount = document.getDouble("totalAmount") ?: 0.0,
                                paymentMethod = document.getString("paymentMethod") ?: "Cash",
                                items = emptyList()
                            )
                            transactions.add(transaction)
                        } catch (e2: Exception) {
                            // Skip this transaction
                        }
                    }
                }
                transactionAdapter.notifyDataSetChanged()
                
                if (transactions.isEmpty()) {
                    Toast.makeText(requireContext(), "Belum ada riwayat transaksi", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Loaded ${transactions.size} transactions", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Gagal memuat riwayat: ${exception.message}", 
                    Toast.LENGTH_SHORT).show()
            }
    }
}