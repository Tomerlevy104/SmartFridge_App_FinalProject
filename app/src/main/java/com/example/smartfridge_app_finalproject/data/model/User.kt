package com.example.smartfridge_app_finalproject.data.model

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val userName: String = "",
    val password: String = "",
    val profileImageUrl: String? = null,
    val uid: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "userName" to userName,
            "profileImageUrl" to profileImageUrl,
            "uid" to uid
        )
    }
}