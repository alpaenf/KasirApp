package com.example.kasir

data class Product(
    val id: String = "",
    val nama: String = "",
    val kategori: String = "",
    val stok: Int = 0,
    val harga: Double = 0.0
)