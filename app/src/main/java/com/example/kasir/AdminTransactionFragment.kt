package com.example.kasir

import com.example.kasir.Transaction
import com.example.kasir.TransactionAdapter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

class AdminTransactionFragment : Fragment() {
    private lateinit var transactionAdapter: TransactionAdapter
    private var transactionList: MutableList<Transaction> = mutableListOf()
    private var transactionListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_transaction, container, false)

        // Toolbar hamburger
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        // RecyclerView setup
        val rvTransactionList = view.findViewById<RecyclerView>(R.id.rvTransactionList)
        rvTransactionList.layoutManager = LinearLayoutManager(requireContext())
        transactionAdapter = TransactionAdapter(transactionList)
        rvTransactionList.adapter = transactionAdapter

        // Empty state
        val emptyState = view.findViewById<LinearLayout>(R.id.empty_state_transaction)
        updateEmptyState(emptyState, rvTransactionList)

        // Firestore listener
        val db = FirebaseFirestore.getInstance()
        transactionListener = db.collection("transactions")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Snackbar.make(view, "Gagal memuat data transaksi", Snackbar.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                transactionList.clear()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val transaction = doc.toObject(Transaction::class.java)?.copy(id = doc.id)
                        if (transaction != null) transactionList.add(transaction)
                    }
                }
                transactionAdapter.notifyDataSetChanged()
                updateEmptyState(emptyState, rvTransactionList)
            }

        // Search
        val etSearch = view.findViewById<EditText>(R.id.etSearchTransaction)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTransaction(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FAB tambah transaksi
        val fabAddTransaction = view.findViewById<FloatingActionButton>(R.id.fabAddTransaction)
        fabAddTransaction.setOnClickListener {
            showAddTransactionDialog(view)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        transactionListener?.remove()
    }

    private fun showAddTransactionDialog(parentView: View) {
        val context = requireContext()
        val inputTotal = EditText(context)
        inputTotal.hint = "Total"
        inputTotal.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        val inputPayment = EditText(context)
        inputPayment.hint = "Metode Pembayaran"
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 24, 48, 0)
        layout.addView(inputTotal)
        layout.addView(inputPayment)
        val dialog = androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle("Tambah Transaksi Baru")
            .setView(layout)
            .setPositiveButton("Tambah") { d, _ ->
                val total = inputTotal.text.toString().trim().toDoubleOrNull() ?: 0.0
                val payment = inputPayment.text.toString().trim()
                if (payment.isEmpty()) {
                    Snackbar.make(parentView, "Metode pembayaran wajib diisi", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val db = FirebaseFirestore.getInstance()
                val transaction = Transaction(timestamp = Date(), totalAmount = total, paymentMethod = payment)
                db.collection("transactions")
                    .add(transaction)
                    .addOnSuccessListener {
                        Snackbar.make(parentView, "Transaksi berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(parentView, "Gagal menambah transaksi", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .create()
        dialog.show()
    }

    private fun filterTransaction(query: String) {
        val filtered = transactionList.filter {
            (it.id.contains(query, ignoreCase = true) ||
            it.paymentMethod.contains(query, ignoreCase = true))
        }
        val rvTransactionList = view?.findViewById<RecyclerView>(R.id.rvTransactionList)
        rvTransactionList?.adapter = TransactionAdapter(filtered)
    }

    private fun updateEmptyState(emptyState: LinearLayout, rv: RecyclerView) {
        if (transactionList.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rv.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rv.visibility = View.VISIBLE
        }
    }
}
