package com.example.kasir

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Transaction(
    // Exclude id from Firestore serialization, as it's the document ID
    @get:Exclude @set:Exclude var id: String = "",
    val timestamp: Date? = null,
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    @get:PropertyName("items") @set:PropertyName(
        "items"
    ) var items: List<OrderItem> = emptyList()
) : Parcelable