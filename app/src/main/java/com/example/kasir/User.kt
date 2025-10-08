package com.example.kasir

import com.google.firebase.firestore.Exclude

data class User(
    @get:Exclude var id: String = "",
    val uid: String = "",
    val email: String = "",
    val nama: String = "",
    val role: String = ""
)