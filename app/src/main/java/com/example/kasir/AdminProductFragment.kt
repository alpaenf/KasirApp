package com.example.kasir

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AdminProductFragment : Fragment(), ProductAdapter.OnProductActionListener {

    private lateinit var productAdapter: ProductAdapter
    private var productList: MutableList<Product> = mutableListOf()
    private var productListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_product, container, false)

        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        val rvProduk = view.findViewById<RecyclerView>(R.id.rvProduk)
        rvProduk.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(productList, this)
        rvProduk.adapter = productAdapter

        // Tombol tambah produk
        val btnTambah = view.findViewById<View>(R.id.btnTambah)
        btnTambah.setOnClickListener {
            showAddProductDialog(view)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = FirebaseFirestore.getInstance()
        productListener = db.collection("products")
            .orderBy("nama", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Snackbar.make(view, "Gagal memuat data produk", Snackbar.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                productList.clear()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val product = doc.toObject(Product::class.java)?.copy(id = doc.id)
                        if (product != null) productList.add(product)
                    }
                }
                productAdapter.notifyDataSetChanged()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        productListener?.remove()
    }

    override fun onEdit(product: Product, position: Int) {
        view?.let { showEditProductDialog(it, product) }
    }

    override fun onDelete(product: Product, position: Int) {
        view?.let { showDeleteProductDialog(it, product) }
    }

    // ========================
    // Dialog Tambah Produk
    // ========================
    fun showAddProductDialog(parentView: View) {
        val context = requireContext()
        val inputNama = EditText(context).apply { hint = "Nama Produk" }
        val inputKategori = EditText(context).apply { hint = "Kategori (bebas)" }
        val inputStok = EditText(context).apply {
            hint = "Stok"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        val inputHarga = EditText(context).apply {
            hint = "Harga"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(inputNama)
            addView(inputKategori)
            addView(inputStok)
            addView(inputHarga)
        }

        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Tambah Produk Baru")
            .setView(layout)
            .setPositiveButton("Tambah") { _, _ ->
                val nama = inputNama.text.toString().trim()
                val kategori = inputKategori.text.toString().trim()
                val stok = inputStok.text.toString().trim().toIntOrNull() ?: 0
                val harga = inputHarga.text.toString().trim().toDoubleOrNull() ?: 0.0

                if (nama.isEmpty() || kategori.isEmpty()) {
                    Snackbar.make(parentView, "Nama dan kategori wajib diisi", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val db = FirebaseFirestore.getInstance()
                val product = Product(nama = nama, kategori = kategori, stok = stok, harga = harga)
                db.collection("products")
                    .add(product)
                    .addOnSuccessListener {
                        Snackbar.make(parentView, "Produk berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(parentView, "Gagal menambah produk", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .create()
        dialog.show()
    }

    // ========================
    // Dialog Edit Produk
    // ========================
    fun showEditProductDialog(parentView: View, product: Product) {
        val context = requireContext()
        val inputNama = EditText(context).apply {
            hint = "Nama Produk"
            setText(product.nama)
        }
        val inputKategori = EditText(context).apply {
            hint = "Kategori"
            setText(product.kategori)
        }
        val inputStok = EditText(context).apply {
            hint = "Stok"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(product.stok.toString())
        }
        val inputHarga = EditText(context).apply {
            hint = "Harga"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(product.harga.toString())
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
            addView(inputNama)
            addView(inputKategori)
            addView(inputStok)
            addView(inputHarga)
        }

        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Edit Produk")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = inputNama.text.toString().trim()
                val kategori = inputKategori.text.toString().trim()
                val stok = inputStok.text.toString().trim().toIntOrNull() ?: 0
                val harga = inputHarga.text.toString().trim().toDoubleOrNull() ?: 0.0

                if (nama.isEmpty() || kategori.isEmpty()) {
                    Snackbar.make(parentView, "Nama dan kategori wajib diisi", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(product.id)
                    .update("nama", nama, "kategori", kategori, "stok", stok, "harga", harga)
                    .addOnSuccessListener {
                        Snackbar.make(parentView, "Produk berhasil diupdate", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(parentView, "Gagal update produk", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .create()
            .show()
    }

    // ========================
    // Dialog Hapus Produk
    // ========================
    fun showDeleteProductDialog(parentView: View, product: Product) {
        val context = requireContext()
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Hapus Produk")
            .setMessage("Yakin ingin menghapus produk ${product.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                val db = FirebaseFirestore.getInstance()
                db.collection("products").document(product.id)
                    .delete()
                    .addOnSuccessListener {
                        Snackbar.make(parentView, "Produk berhasil dihapus", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(parentView, "Gagal hapus produk", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .create()
            .show()
    }
}
