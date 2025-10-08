package com.example.kasir

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(
    private var userList: MutableList<User>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private var userListFull: MutableList<User> = mutableListOf()

    init {
        userListFull.addAll(userList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int = userList.size

    fun updateData(newUsers: List<User>) {
        userList.clear()
        userList.addAll(newUsers)
        userListFull.clear()
        userListFull.addAll(newUsers)
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        userList.clear()
        if (query.isEmpty()) {
            userList.addAll(userListFull)
        } else {
            val filteredList = userListFull.filter {
                it.nama.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true)
            }
            userList.addAll(filteredList)
        }
        notifyDataSetChanged()
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvUserName: TextView = itemView.findViewById(R.id.tvUserName)
        private val tvUserEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        private val tvUserRole: TextView = itemView.findViewById(R.id.tvUserRole)

        fun bind(user: User) {
            tvUserName.text = user.nama
            tvUserEmail.text = user.email
            tvUserRole.text = user.role
        }
    }
}
