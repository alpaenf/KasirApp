package com.example.kasir

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class AddProductActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_product)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val cardImagePicker = findViewById<View>(R.id.cardImagePicker)
        val ivProdukPreview = findViewById<ImageView>(R.id.ivProdukPreview)
        val ivCameraIcon = findViewById<ImageView>(R.id.ivCameraIcon)

        cardImagePicker.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent, "Pilih Gambar Produk"), PICK_IMAGE_REQUEST)
        }

        val etNamaProduk = findViewById<EditText>(R.id.etNamaProduk)
        // Kategori: gunakan EditText, bukan spinner
        val etHarga = findViewById<EditText>(R.id.etHarga)
        val etStok = findViewById<EditText>(R.id.etStok)
        val btnSimpan = findViewById<View>(R.id.btnSimpan)
        val btnBatal = findViewById<View>(R.id.btnBatal)

        btnBatal.setOnClickListener { finish() }

        btnSimpan.setOnClickListener { v ->
            val nama = etNamaProduk.text.toString().trim()
            val kategori = findViewById<EditText>(R.id.spinnerKategori)?.text?.toString()?.trim() ?: ""
            val harga = etHarga.text.toString().trim()
            val stok = etStok.text.toString().trim()
            if (nama.isEmpty() || kategori.isEmpty() || harga.isEmpty() || stok.isEmpty()) {
                Snackbar.make(v, "Semua field wajib diisi", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (selectedImageUri == null) {
                Snackbar.make(v, "Pilih gambar produk terlebih dahulu", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImageAndSaveProduct(nama, kategori, harga, stok, selectedImageUri!!, v)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data
            val ivProdukPreview = findViewById<ImageView>(R.id.ivProdukPreview)
            val ivCameraIcon = findViewById<ImageView>(R.id.ivCameraIcon)
            ivProdukPreview.setImageURI(selectedImageUri)
            ivProdukPreview.visibility = View.VISIBLE
            ivCameraIcon.visibility = View.GONE
        }
    }

    private fun uploadImageAndSaveProduct(nama: String, kategori: String, harga: String, stok: String, imageUri: Uri, parentView: View) {
        val storageRef = FirebaseStorage.getInstance().reference.child("product_images/${System.currentTimeMillis()}_${nama}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    saveProductToFirestore(nama, kategori, harga, stok, uri.toString(), parentView)
                }.addOnFailureListener { _ ->
                    Snackbar.make(parentView, "Gagal mendapatkan URL gambar", Snackbar.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { _ ->
                Snackbar.make(parentView, "Gagal upload gambar", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun saveProductToFirestore(nama: String, kategori: String, harga: String, stok: String, fotoUrl: String, parentView: View) {
        val db = FirebaseFirestore.getInstance()
        val product = hashMapOf(
            "nama" to nama,
            "kategori" to kategori,
            "harga" to harga,
            "stok" to stok,
            "fotoUrl" to fotoUrl
        )
        db.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Snackbar.make(parentView, "Gagal menambah produk", Snackbar.LENGTH_SHORT).show()
            }
    }
}
