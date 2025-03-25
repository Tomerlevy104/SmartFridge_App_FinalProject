//package com.example.smartfridge_app_finalproject.adapters
//
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import com.example.smartfridge_app_finalproject.R
//import com.example.smartfridge_app_finalproject.data.model.User
//import com.google.android.material.imageview.ShapeableImageView
//import com.google.android.material.materialswitch.MaterialSwitch
//import com.google.android.material.textview.MaterialTextView
//
//class UserAdapter(
//    private var users: MutableList<User>,
//    private val onUserStatusChanged: (User, Boolean) -> Unit
//) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {
//
//    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val userImageProfile: ShapeableImageView = itemView.findViewById(R.id.item_user_IMG_avatar)
//        private val userFName: MaterialTextView = itemView.findViewById(R.id.item_user_TV_name)
//
//        private val userSwitch: MaterialSwitch = itemView.findViewById(R.id.item_user_SW_active)
//
//        fun bind(user: User) {
//            userFName.text = user.firstName
//
//            //Upload profile picture
//            //If no URL, use default image
//            if (user.profileImageUri==null) {
//                userImageProfile.setImageResource(R.drawable.profile_man)
//            } else {
//                //Load image from URL using Glide
//                Glide.with(itemView.context)
//                    .load(user.profileImageUri)
//                    .placeholder(R.drawable.profile_man)
//                    .error(R.drawable.profile_man)
//                    .into(userImageProfile)
//            }
//
//            userSwitch.setOnCheckedChangeListener { _, isChecked ->
//                onUserStatusChanged(user, isChecked)
//            }
//        }
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_user, parent, false)
//        return UserViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
//        holder.bind(users[position])
//    }
//
//    override fun getItemCount() = users.size
//
//    fun updateUsers(newUsers: List<User>) {
//        users.clear()
//        users.addAll(newUsers)
//        notifyDataSetChanged()
//    }
//}