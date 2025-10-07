package com.example.kasir

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class JuiceMenuCrudActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rvJuiceMenu: RecyclerView
    private lateinit var fabAddJuice: FloatingActionButton
    private lateinit var juiceMenuAdapter: JuiceMenuCrudAdapter
    private val juiceList = mutableListOf<JuiceItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_juice_menu_crud)

        db = FirebaseFirestore.getInstance()

        rvJuiceMenu = findViewById(R.id.rvJuiceMenu)
        fabAddJuice = findViewById(R.id.fabAddJuice)

        rvJuiceMenu.layoutManager = LinearLayoutManager(this)
        juiceMenuAdapter = JuiceMenuCrudAdapter(juiceList, this::showEditJuiceDialog, this::deleteJuiceItem)
        rvJuiceMenu.adapter = juiceMenuAdapter

        fabAddJuice.setOnClickListener {
            showAddJuiceDialog()
        }

        fetchJuiceMenu()
    }

    private fun fetchJuiceMenu() {
        db.collection("juice_menu")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                juiceList.clear()
                if (snapshots != null) {
                    for (document in snapshots) {
                        val juiceItem = document.toObject(JuiceItem::class.java).copy(id = document.id)
                        juiceList.add(juiceItem)
                    }
                }
                juiceMenuAdapter.notifyDataSetChanged()
            }
    }

    private fun showAddJuiceDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_juice, null)
        val etJuiceName = dialogView.findViewById<EditText>(R.id.etJuiceName)
        val etJuicePrice = dialogView.findViewById<EditText>(R.id.etJuicePrice)

        AlertDialog.Builder(this)
            .setTitle("Tambah Menu Jus")
            .setView(dialogView)
            .setPositiveButton("Tambah") { _, _ ->
                val name = etJuiceName.text.toString()
                val price = etJuicePrice.text.toString().toDoubleOrNull() ?: 0.0
                if (name.isNotEmpty()) {
                    addJuiceItem(name, price)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditJuiceDialog(juiceItem: JuiceItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_juice, null)
        val etJuiceName = dialogView.findViewById<EditText>(R.id.etJuiceName)
        val etJuicePrice = dialogView.findViewById<EditText>(R.id.etJuicePrice)

        etJuiceName.setText(juiceItem.name)
        etJuicePrice.setText(juiceItem.price.toString())

        AlertDialog.Builder(this)
            .setTitle("Edit Menu Jus")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val newName = etJuiceName.text.toString()
                val newPrice = etJuicePrice.text.toString().toDoubleOrNull() ?: 0.0
                if (newName.isNotEmpty()) {
                    updateJuiceItem(juiceItem.id, newName, newPrice)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun addJuiceItem(name: String, price: Double) {
        val juiceItem = hashMapOf("name" to name, "price" to price)
        db.collection("juice_menu").add(juiceItem)
    }

    private fun updateJuiceItem(id: String, newName: String, newPrice: Double) {
        db.collection("juice_menu").document(id)
            .update("name", newName, "price", newPrice)
    }

    private fun deleteJuiceItem(juiceItem: JuiceItem) {
        db.collection("juice_menu").document(juiceItem.id).delete()
    }
}
