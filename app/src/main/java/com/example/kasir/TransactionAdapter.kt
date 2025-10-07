package com.example.kasir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kasir.databinding.ItemTransactionBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TransactionAdapter(
    private val transactions: List<Transaction>
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.bind(transaction)
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(private val binding: ItemTransactionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            binding.tvTransactionId.text = "ID: ${transaction.id}"
            if (transaction.timestamp != null) {
                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                binding.tvTransactionTime.text = sdf.format(transaction.timestamp)
            } else {
                binding.tvTransactionTime.text = "No timestamp"
            }
            binding.tvTransactionTotal.text = "Total: Rp${transaction.totalAmount.toInt()}"
            binding.tvTransactionPaymentMethod.text = "Metode: ${transaction.paymentMethod}"

            val itemsString = transaction.items.joinToString(separator = ", ") { "${it.juiceItem.name} (${it.quantity})" }
            binding.tvTransactionItems.text = "Item: $itemsString"
        }
    }
}
