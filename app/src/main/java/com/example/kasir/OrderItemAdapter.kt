package com.example.kasir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kasir.databinding.ItemOrderBinding

class OrderItemAdapter(
    private val orderItems: MutableList<OrderItem>,
    private val onQuantityChanged: () -> Unit,
    private val onItemRemoved: (OrderItem) -> Unit // Callback baru
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val orderItem = orderItems[position]
        holder.bind(orderItem)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class OrderItemViewHolder(private val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(orderItem: OrderItem) {
            binding.tvOrderItemName.text = orderItem.juiceItem.name
            binding.tvOrderItemQuantity.text = orderItem.quantity.toString()

            binding.btnIncreaseQuantity.setOnClickListener {
                orderItem.quantity++
                notifyItemChanged(adapterPosition)
                onQuantityChanged()
            }

            binding.btnDecreaseQuantity.setOnClickListener {
                if (orderItem.quantity > 0) {
                    orderItem.quantity--
                    if (orderItem.quantity == 0) {
                        // Pastikan posisi valid sebelum menghapus
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            val removedItem = orderItems.removeAt(adapterPosition)
                            notifyItemRemoved(adapterPosition)
                            onItemRemoved(removedItem) // Panggil callback baru dengan item yang dihapus
                        }
                    } else {
                        notifyItemChanged(adapterPosition)
                        onQuantityChanged()
                    }
                }
            }
        }
    }
}
