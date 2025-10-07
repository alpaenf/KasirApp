package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JuiceMenuKasirAdapter(
    private val menuList: List<JuiceItem>,
    private val onAddClick: (JuiceItem) -> Unit
) : RecyclerView.Adapter<JuiceMenuKasirAdapter.JuiceMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuiceMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_juice_menu_kasir, parent, false)
        return JuiceMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: JuiceMenuViewHolder, position: Int) {
        val juiceItem = menuList[position]
        holder.bind(juiceItem)
    }

    override fun getItemCount(): Int = menuList.size

    inner class JuiceMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJuiceName: TextView = itemView.findViewById(R.id.tvJuiceName)
        private val tvJuicePrice: TextView = itemView.findViewById(R.id.tvJuicePrice)
        private val btnAddOne: Button = itemView.findViewById(R.id.btnAddOne)

        fun bind(juiceItem: JuiceItem) {
            tvJuiceName.text = juiceItem.name
            tvJuicePrice.text = "Rp${juiceItem.price.toInt()}"

            btnAddOne.setOnClickListener { onAddClick(juiceItem) }
        }
    }
}