package com.example.kasir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kasir.databinding.ItemJuiceMenuBinding

class JuiceMenuAdapter(
    private val menuList: List<JuiceItem>,
    private val onEditClick: (JuiceItem) -> Unit,
    private val onDeleteClick: (JuiceItem) -> Unit
) : RecyclerView.Adapter<JuiceMenuAdapter.JuiceMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuiceMenuViewHolder {
        val binding = ItemJuiceMenuBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return JuiceMenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JuiceMenuViewHolder, position: Int) {
        val juiceItem = menuList[position]
        holder.bind(juiceItem)
    }

    override fun getItemCount(): Int = menuList.size

    inner class JuiceMenuViewHolder(private val binding: ItemJuiceMenuBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(juiceItem: JuiceItem) {
            binding.tvJuiceName.text = juiceItem.name
            binding.tvJuicePrice.text = "Rp${juiceItem.price.toInt()}"

            binding.btnEditMenu.setOnClickListener { onEditClick(juiceItem) }
            binding.btnDeleteMenu.setOnClickListener { onDeleteClick(juiceItem) }
        }
    }
}
