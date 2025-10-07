package com.example.kasir

data class CartItem(
    val productName: String,
    val price: String,
    val quantity: Int,
    val productId: String = "",
    val imageUrl: String = ""
)