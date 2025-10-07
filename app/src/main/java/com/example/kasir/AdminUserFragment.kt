package com.example.kasir

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class AdminUserFragment : Fragment() {
    private lateinit var userAdapter: UserAdapter
    private var userList: MutableList<User> = mutableListOf()
    private var userListener: ListenerRegistration? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_user, container, false)

        // Toolbar hamburger
        val toolbar = view.findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener {
            (activity as? AdminDashboardActivity)?.openDrawer()
        }

        // RecyclerView setup
        val rvUserList = view.findViewById<RecyclerView>(R.id.rvUserList)
        rvUserList.layoutManager = LinearLayoutManager(requireContext())
    userAdapter = UserAdapter(userList)
        rvUserList.adapter = userAdapter

        // Firestore listener
        val db = FirebaseFirestore.getInstance()
        userListener = db.collection("users")
            .orderBy("nama", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Snackbar.make(view, "Gagal memuat data pengguna", Snackbar.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                userList.clear()
                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val user = doc.toObject(User::class.java)
                        if (user != null) userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

        // Search
        val etSearch = view.findViewById<EditText>(R.id.etSearchUser)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUser(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // FAB tambah pengguna
        val fabAddUser = view.findViewById<FloatingActionButton>(R.id.fabAddUser)
        fabAddUser.setOnClickListener {
            showAddUserDialog(view)
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        userListener?.remove()
    }



    private fun showAddUserDialog(parentView: View) {
        val context = requireContext()
        val inputEmail = EditText(context)
        inputEmail.hint = "Email"
        val inputRole = EditText(context)
        inputRole.hint = "Role (admin/kasir)"
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(48, 24, 48, 0)
        layout.addView(inputEmail)
        layout.addView(inputRole)
        val dialog = AlertDialog.Builder(context)
            .setTitle("Tambah Pengguna Baru")
            .setView(layout)
            .setPositiveButton("Tambah") { d, _ ->
                val email = inputEmail.text.toString().trim()
                val role = inputRole.text.toString().trim()
                if (email.isEmpty() || role.isEmpty()) {
                    Snackbar.make(parentView, "Semua field wajib diisi", Snackbar.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val db = FirebaseFirestore.getInstance()
                val user = User(email = email, role = role)
                db.collection("users")
                    .add(user)
                    .addOnSuccessListener {
                        Snackbar.make(parentView, "Pengguna berhasil ditambahkan", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Snackbar.make(parentView, "Gagal menambah pengguna", Snackbar.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .create()
        dialog.show()
    }

    // Edit user dialog di-nonaktifkan karena field nama dan id tidak ada di model User

    // Delete user dialog di-nonaktifkan karena field id tidak ada di model User

    private fun filterUser(query: String) {
        val filtered = userList.filter {
            it.email.contains(query, ignoreCase = true) ||
            it.role.contains(query, ignoreCase = true)
        }
        userAdapter = UserAdapter(filtered)
        view?.findViewById<RecyclerView>(R.id.rvUserList)?.adapter = userAdapter
    }
}
