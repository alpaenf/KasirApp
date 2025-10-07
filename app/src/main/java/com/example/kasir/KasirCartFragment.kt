package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class KasirCartFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_kasir_cart, container, false)
        val rvCart = view.findViewById<RecyclerView>(R.id.rvCart)
        
        // Setup RecyclerView
        rvCart.layoutManager = LinearLayoutManager(context)
        rvCart.setHasFixedSize(true)
        
        // Dummy data untuk cart
        val cartItems = listOf(
            CartItem("Es Jeruk", "Rp 10.000", 2),
            CartItem("Es Teh", "Rp 5.000", 1),
            CartItem("Jus Mangga", "Rp 12.000", 1)
        )
        val adapter = CartAdapter(cartItems)
        rvCart.adapter = adapter
        
        return view
    }
}
