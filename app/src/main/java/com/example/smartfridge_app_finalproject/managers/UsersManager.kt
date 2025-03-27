package com.example.smartfridge_app_finalproject.managers

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
* Class for managing users registered for the application
*/
class UsersManager(private val context: Context) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    fun addNewUser(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        profileImageUri: Uri? = null
    ) {
        auth.createUserWithEmailAndPassword(email, password) // Here the user is created
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser
                    val userProfileImageUri = profileImageUri
                        ?: Uri.parse("android.resource://${context.packageName}/${R.drawable.profile_man}")

                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        password = "",  //I don't save the password in the firestore
                        profileImageUri = userProfileImageUri,
                        uid = firebaseUser?.uid ?: ""
                    )

                    //Add user to firestore
                    firestore.collection("users")
                        .document(firebaseUser?.uid ?: "")
                        .set(user.toMap())
                        .addOnSuccessListener {
                            showToast(context.getString(R.string.registration_success))
                            //Navigate to main activity
                            val intent = Intent(context, MainActivity::class.java)
                            //Clear all the previous activities
                            intent.flags =
                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            context.startActivity(intent)
                        }
                        .addOnFailureListener {
                            showToast(context.getString(R.string.registration_failed))
                        }
                } else {
                    showToast(context.getString(R.string.user_name_already_exist))
                }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}