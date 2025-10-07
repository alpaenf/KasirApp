package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private val productList: List<Product>,
    private val listener: OnProductActionListener
    
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    interface OnProductActionListener {
        fun onEdit(product: Product, position: Int)
        fun onDelete(product: Product, position: Int)
    }

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNama: TextView = itemView.findViewById(R.id.tvNamaProduk)
        val tvKategori: TextView = itemView.findViewById(R.id.tvKategoriProduk)
        val tvStok: TextView = itemView.findViewById(R.id.tvStokProduk)
        val tvHarga: TextView = itemView.findViewById(R.id.tvHargaProduk)
        val btnEdit: View = itemView.findViewById(R.id.btnEdit)
        val btnDelete: View = itemView.findViewById(R.id.btnHapus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_management, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.tvNama.text = product.nama
        holder.tvKategori.text = product.kategori
        holder.tvStok.text = product.stok.toString()
        holder.tvHarga.text = product.harga.toString()
        holder.btnEdit.setOnClickListener { listener.onEdit(product, position) }
        holder.btnDelete.setOnClickListener { listener.onDelete(product, position) }
    }

    override fun getItemCount(): Int = productList.size
}
