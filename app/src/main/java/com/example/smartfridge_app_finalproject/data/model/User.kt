package com.example.smartfridge_app_finalproject.data.model

import android.net.Uri

/**
 * User model
 */
data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val password: String = "",
    val profileImageUri: Uri? = null,
    val uid: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "profileImageUrl" to profileImageUri,
            "uid" to uid
        )
    }
}