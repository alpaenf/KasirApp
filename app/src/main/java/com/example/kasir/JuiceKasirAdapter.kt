package com.example.kasir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kasir.databinding.ItemJuiceKasirBinding

class JuiceKasirAdapter(
    private val juiceList: List<JuiceItem>,
    private val onQuantityChange: (JuiceItem, Int) -> Unit
) : RecyclerView.Adapter<JuiceKasirAdapter.JuiceViewHolder>() {

    private val quantities = mutableMapOf<String, Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuiceViewHolder {
        val binding = ItemJuiceKasirBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JuiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JuiceViewHolder, position: Int) {
        val juiceItem = juiceList[position]
        holder.bind(juiceItem)
    }

    override fun getItemCount(): Int = juiceList.size

    fun resetQuantities() {
        quantities.clear()
        notifyDataSetChanged()
    }

    fun updateQuantity(juiceItem: JuiceItem, newQuantity: Int) {
        quantities[juiceItem.id] = newQuantity
        val position = juiceList.indexOfFirst { it.id == juiceItem.id }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    inner class JuiceViewHolder(private val binding: ItemJuiceKasirBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(juiceItem: JuiceItem) {
            binding.tvJuiceName.text = juiceItem.name
            binding.tvJuicePrice.text = "Rp${juiceItem.price.toInt()}"

            var quantity = quantities.getOrPut(juiceItem.id) { 0 }
            binding.tvQuantity.text = quantity.toString()

            binding.btnIncreaseQuantity.setOnClickListener {
                quantity++
                binding.tvQuantity.text = quantity.toString()
                quantities[juiceItem.id] = quantity
                onQuantityChange(juiceItem, quantity)
            }

            binding.btnDecreaseQuantity.setOnClickListener {
                if (quantity > 0) {
                    quantity--
                    binding.tvQuantity.text = quantity.toString()
                    quantities[juiceItem.id] = quantity
                    onQuantityChange(juiceItem, quantity)
                }
            }
        }
    }
}
