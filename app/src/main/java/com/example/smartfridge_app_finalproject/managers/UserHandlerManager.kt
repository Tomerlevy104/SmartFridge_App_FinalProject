package com.example.smartfridge_app_finalproject.managers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

/**
 * class for managing one user
 */
class UserHandlerManager private constructor() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var userData: UserData? = null

    //Data class to hold user information
    data class UserData(
        val uid: String,
        val firstName: String,
        val lastName: String,
        val profileImageUrl: String?,
        val email: String?
    )

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get Firebase current user object
    fun getCurrentFirebaseUser(): FirebaseUser? {
        return auth.currentUser
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Get current user data
    fun getCurrentUserData(): UserData? {
        return userData
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Load user profile data from firestore
    fun loadUserProfile(onComplete: (Result<UserData>) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(Result.failure(Exception("no user logged in")))
            return
        }

        firestore.collection("users")
            .document(currentUser.uid).get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: "guest"
                    val lastName = document.getString("lastName") ?: "new"
                    val profileImageUrl = document.getString("profileImageUrl")
                    val userData = UserData(
                        uid = currentUser.uid,
                        firstName = firstName,
                        lastName = lastName,
                        profileImageUrl = profileImageUrl,
                        email = currentUser.email
                    )

                    this.userData = userData
                    onComplete(Result.success(userData))
                } else {
                    onComplete(Result.failure(Exception("No user data found")))
                }
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Update user profile data
    fun updateUserProfile(
        firstName: String,
        lastName: String,
        profileImageUrl: String?,
        onComplete: (Result<Unit>) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onComplete(Result.failure(Exception("No user logged in")))
            return
        }

        val updates = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName
        )
        profileImageUrl?.let { updates["profileImageUrl"] = it }

        firestore.collection("users")
            .document(currentUser.uid)
            .update(updates) //Update firestore
            .addOnSuccessListener { //If success to update details in firestore -> Update local details
                // Update local cache
                userData = userData?.copy(
                    firstName = firstName,
                    lastName = lastName,
                    profileImageUrl = profileImageUrl ?: userData?.profileImageUrl
                )
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Clear user data on logout
    fun clearUserData() {
        userData = null
        auth.signOut()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Function to log in a user with email and password
    fun loginUser(email: String, password: String, onComplete: (Result<FirebaseUser>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onComplete(Result.success(user))
                    } else {
                        onComplete(Result.failure(Exception("login failed - no user data")))
                    }
                } else {
                    onComplete(Result.failure(task.exception ?: Exception("login failed")))
                }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Function to get user information by UID
    fun getUserData(uid: String, onComplete: (Result<UserData>) -> Unit) {
        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val firstName = document.getString("firstName") ?: "guest"
                    val lastName = document.getString("lastName") ?: "new"
                    val profileImageUrl = document.getString("profileImageUrl")
                    val userData = UserData(
                        uid = uid,
                        firstName = firstName,
                        lastName = lastName,
                        profileImageUrl = profileImageUrl,
                        email = auth.currentUser?.email
                    )

                    this.userData = userData
                    onComplete(Result.success(userData))
                } else {
                    onComplete(Result.failure(Exception("no details about user")))
                }
            }
            .addOnFailureListener { e ->
                onComplete(Result.failure(e))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // Function to send password reset email
    fun sendPasswordResetEmail(email: String, onComplete: (Result<Unit>) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(Result.success(Unit))
                } else {
                    onComplete(Result.failure(task.exception ?: Exception("Email sending failed")))
                }
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    companion object {
        @Volatile
        private var instance: UserHandlerManager? = null

        fun getInstance(): UserHandlerManager {
            return instance ?: synchronized(this) {
                instance ?: UserHandlerManager().also { instance = it }
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
}