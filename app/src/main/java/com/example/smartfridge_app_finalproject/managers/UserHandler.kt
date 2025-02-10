package com.example.smartfridge_app_finalproject.managers

import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserHandler {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    //Function to load user firstName
    private fun loadUserFirsName(): String?  {
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // נשלוף את הנתונים מה-document
                        val firstName = document.getString("firstName")

                        //לשים פה בהמשך את המוצרים גם וכו?

                    }
                }
        }
    }

    //Function to load user image
    private fun loadUserImage() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            firestore.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        // נשלוף את הנתונים מה-document
                        val profileImageUrl = document.getString("profileImageUrl")
                    }
                }
        }
    }
}