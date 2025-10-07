package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class JuiceMenuCrudAdapter(
    private val juiceList: MutableList<JuiceItem>,
    private val onEditClick: (JuiceItem) -> Unit,
    private val onDeleteClick: (JuiceItem) -> Unit
) : RecyclerView.Adapter<JuiceMenuCrudAdapter.JuiceMenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuiceMenuViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_juice_menu_admin, parent, false)
        return JuiceMenuViewHolder(view)
    }

    override fun onBindViewHolder(holder: JuiceMenuViewHolder, position: Int) {
        val juiceItem = juiceList[position]
        holder.bind(juiceItem)
    }

    override fun getItemCount(): Int = juiceList.size

    fun updateData(newJuiceList: List<JuiceItem>) {
        juiceList.clear()
        juiceList.addAll(newJuiceList)
        notifyDataSetChanged()
    }

    inner class JuiceMenuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvJuiceName: TextView = itemView.findViewById(R.id.tvJuiceName)
        private val tvJuicePrice: TextView = itemView.findViewById(R.id.tvJuicePrice)
        private val btnEdit: Button = itemView.findViewById(R.id.btnEdit)
        private val btnDelete: Button = itemView.findViewById(R.id.btnDelete)

        fun bind(juiceItem: JuiceItem) {
            tvJuiceName.text = juiceItem.name
            tvJuicePrice.text = "Rp${juiceItem.price.toInt()}"

            btnEdit.setOnClickListener { onEditClick(juiceItem) }
            btnDelete.setOnClickListener { onDeleteClick(juiceItem) }
        }
    }
}