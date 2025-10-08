package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.*

class OrderItemAdapter(
    private val orderItems: MutableList<OrderItem>,
    private val onQuantityChanged: () -> Unit,
    private val onItemRemoved: (OrderItem) -> Unit
) : RecyclerView.Adapter<OrderItemAdapter.OrderItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order, parent, false)
        return OrderItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderItemViewHolder, position: Int) {
        val orderItem = orderItems[position]
        holder.bind(orderItem)
    }

    override fun getItemCount(): Int = orderItems.size

    inner class OrderItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textItemName: TextView = itemView.findViewById(R.id.textItemName)
        private val textItemQuantity: TextView = itemView.findViewById(R.id.textItemQuantity)
        private val textItemPrice: TextView = itemView.findViewById(R.id.textItemPrice)
        private val buttonRemoveItem: MaterialButton = itemView.findViewById(R.id.buttonRemoveItem)
        
        fun bind(orderItem: OrderItem) {
            val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            
            textItemName.text = orderItem.juiceItem.name
            textItemQuantity.text = "Qty: ${orderItem.quantity}"
            textItemPrice.text = formatter.format(orderItem.totalPrice)

            buttonRemoveItem.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    val removedItem = orderItems.removeAt(adapterPosition)
                    notifyItemRemoved(adapterPosition)
                    onItemRemoved(removedItem)
                    onQuantityChanged()
                }
            }
        }
    }
}
