package com.example.kasir

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class OrderItem(
    val juiceItem: JuiceItem,
    var quantity: Int = 1
) : Parcelable {
    val subtotal: Double
        get() = juiceItem.price * quantity
}
