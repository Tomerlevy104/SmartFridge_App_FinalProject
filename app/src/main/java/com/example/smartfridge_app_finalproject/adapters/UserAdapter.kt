package com.example.smartfridge_app_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.User
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textview.MaterialTextView

class UserAdapter(
    private var users: MutableList<User>,
    private val onUserStatusChanged: (User, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val userImageProfile: ShapeableImageView = itemView.findViewById(R.id.item_user_IMG_avatar)
        private val userFName: MaterialTextView = itemView.findViewById(R.id.item_user_TV_name)
        private val userUserName: MaterialTextView = itemView.findViewById(R.id.item_user_TV_username)

        private val userSwitch: MaterialSwitch = itemView.findViewById(R.id.item_user_SW_active)

        fun bind(user: User) {
            userFName.text = user.firstName
//            userLName.text = user.lastName
            userUserName.text = user.userName
            userImageProfile.setImageResource(user.imageResourceId)
            //userSwitch.isChecked = user.isActive

            userSwitch.setOnCheckedChangeListener { _, isChecked ->
                onUserStatusChanged(user, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    fun updateUsers(newUsers: List<User>) {
        users.clear()
        users.addAll(newUsers)
        notifyDataSetChanged()
    }
}