package com.example.kasir

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class JuiceItem(
    @get:Exclude
    var id: String = "", // ID dokumen dari Firestore
    var name: String = "",
    var price: Double = 0.0
) : Parcelable
