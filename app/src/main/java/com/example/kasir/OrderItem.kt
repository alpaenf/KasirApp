package com.example.kasir

data class OrderItem(
    var juiceItem: JuiceItem = JuiceItem(),
    var quantity: Int = 1
) {
    val totalPrice: Double
        get() = juiceItem.price * quantity
}
