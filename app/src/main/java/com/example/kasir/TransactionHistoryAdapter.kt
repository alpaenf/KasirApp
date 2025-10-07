package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.kasir.R // Import eksplisit untuk R
import com.example.kasir.Transaction // Import eksplisit untuk Transaction

class TransactionHistoryAdapter(
    private val transactionList: List<Transaction>
) : RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactionList[position]
        holder.bind(currentTransaction)
    }

    override fun getItemCount() = transactionList.size

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTransactionId: TextView = itemView.findViewById(R.id.tvTransactionId)
        private val tvTransactionTime: TextView = itemView.findViewById(R.id.tvTransactionTime)
        private val tvTransactionTotal: TextView = itemView.findViewById(R.id.tvTransactionTotal)
        private val tvTransactionPaymentMethod: TextView = itemView.findViewById(R.id.tvTransactionPaymentMethod)
        private val tvTransactionItems: TextView = itemView.findViewById(R.id.tvTransactionItems)

        fun bind(transaction: Transaction) {
            tvTransactionId.text = transaction.id.substring(0, 8) // Show a shorter ID
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            tvTransactionTime.text = transaction.timestamp?.let { dateFormat.format(it) } ?: "N/A"
            
            tvTransactionTotal.text = "Rp ${transaction.totalAmount.toInt()}"
            tvTransactionPaymentMethod.text = transaction.paymentMethod

            val itemsDetails = transaction.items.joinToString("\n") {
                "- ${it.juiceItem.name} x${it.quantity}"
            }
            tvTransactionItems.text = itemsDetails
        }
    }
}
