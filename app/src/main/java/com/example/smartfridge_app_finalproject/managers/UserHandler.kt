package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.User

class UserHandler {
    private var user: User? = null

    fun createUser(
        firstName: String,
        lastName: String,
        email: String,
        userName: String,
        password: String,
        imageResourceId: Int = R.drawable.profile_man
    ): Boolean {
        if (isValidUserDetails(firstName, lastName, email, userName, password)) {
            user = User(firstName, lastName, email, userName, password, imageResourceId)
            return true
        }
        return false
    }

    fun getUser(): User? = user

    fun updateUserDetails(
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        userName: String? = null,
        password: String? = null,
        imageResourceId: Int? = null
    ): Boolean {
        user?.let { currentUser ->
            user = currentUser.copy(
                firstName = firstName ?: currentUser.firstName,
                lastName = lastName ?: currentUser.lastName,
                Email = email ?: currentUser.Email,
                userName = userName ?: currentUser.userName,
                password = password ?: currentUser.password,
                imageResourceId = imageResourceId ?: currentUser.imageResourceId
            )
            return true
        }
        return false
    }

    fun clearUser() {
        user = null
    }

    //Check valid user
    private fun isValidUserDetails(
        firstName: String,
        lastName: String,
        email: String,
        userName: String,
        password: String
    ): Boolean {
        return firstName.isNotBlank() &&
                lastName.isNotBlank() &&
                email.isNotBlank() &&
                userName.isNotBlank() &&
                password.length >= 6
    }

}