package com.example.kasir

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.kasir.databinding.ItemJuiceKasirBinding

class MenuAdapterKasir(
    private val menuList: List<JuiceItem>,
    private val onItemClick: (JuiceItem) -> Unit
) : RecyclerView.Adapter<MenuAdapterKasir.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        val binding = ItemJuiceKasirBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val juiceItem = menuList[position]
        holder.bind(juiceItem)
    }

    override fun getItemCount(): Int = menuList.size

    inner class MenuViewHolder(private val binding: ItemJuiceKasirBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(juiceItem: JuiceItem) {
            binding.tvJuiceName.text = juiceItem.name
            binding.tvJuicePrice.text = "Rp${juiceItem.price.toInt()}"
            itemView.setOnClickListener { onItemClick(juiceItem) }
        }
    }
}
