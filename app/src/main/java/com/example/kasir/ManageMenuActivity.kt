package com.example.kasir

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kasir.databinding.ActivityManageMenuBinding
import com.google.firebase.firestore.FirebaseFirestore

class ManageMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageMenuBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var menuAdapter: JuiceMenuAdapter
    private val menuList = mutableListOf<JuiceItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        fetchMenu()

        binding.btnAddMenu.setOnClickListener {
            showAddEditMenuDialog(null)
        }
    }

    private fun setupRecyclerView() {
        menuAdapter = JuiceMenuAdapter(
            menuList,
            onEditClick = { juiceItem -> showAddEditMenuDialog(juiceItem) },
            onDeleteClick = { juiceItem -> deleteMenu(juiceItem) }
        )
        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(this@ManageMenuActivity)
            adapter = menuAdapter
        }
    }

    private fun fetchMenu() {
        firestore.collection("menu")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    menuList.clear()
                    for (document in snapshots) {
                        val juiceItem = document.toObject(JuiceItem::class.java)
                        juiceItem.id = document.id
                        menuList.add(juiceItem)
                    }
                    menuAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showAddEditMenuDialog(juiceItem: JuiceItem?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_edit_menu, null)
        val etJuiceName = dialogView.findViewById<EditText>(R.id.etJuiceName)
        val etJuicePrice = dialogView.findViewById<EditText>(R.id.etJuicePrice)

        val dialogTitle = if (juiceItem == null) "Tambah Menu" else "Edit Menu"
        if (juiceItem != null) {
            etJuiceName.setText(juiceItem.name)
            etJuicePrice.setText(juiceItem.price.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setView(dialogView)
            .setPositiveButton(if (juiceItem == null) "Tambah" else "Simpan") { dialog, _ ->
                val name = etJuiceName.text.toString()
                val price = etJuicePrice.text.toString().toDoubleOrNull() ?: 0.0

                if (name.isNotEmpty()) {
                    if (juiceItem == null) {
                        addMenu(name, price)
                    } else {
                        updateMenu(juiceItem.id, name, price)
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Batal") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }

    private fun addMenu(name: String, price: Double) {
        val newItem = JuiceItem(name = name, price = price)
        firestore.collection("menu").add(newItem)
    }

    private fun updateMenu(id: String, name: String, price: Double) {
        firestore.collection("menu").document(id).update("name", name, "price", price)
    }

    private fun deleteMenu(juiceItem: JuiceItem) {
        firestore.collection("menu").document(juiceItem.id).delete()
    }
}
