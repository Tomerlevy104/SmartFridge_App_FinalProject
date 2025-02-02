package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.data.model.User
import com.example.smartfridge_app_finalproject.data.repository.UsersRepository

class UsersManager private constructor() {

    private var usersRepository = UsersRepository() //Users repository
    private val users = mutableListOf<User>()

    private var currentUser: User? = null


    init {
        //Initialize users list with all users from repository
        usersRepository.getInitialUsers().forEach { user ->
            addUser(user)
        }
    }

    fun addUser(user: User): Boolean {
        //Checking id user exist
        if (users.any { it.Email == user.Email || it.userName == user.userName }) {
            return false
        }
        users.add(user)// פה לממש את ההוספה לפייר בייס
        return true
    }

    fun removeUser(userName: String): Boolean {
        return users.removeIf { it.userName == userName }
    }

    fun updateUser(oldUser: User, newUser: User): Boolean {
        val index = users.indexOf(oldUser)
        if (index != -1) {
            users[index] = newUser
            return true
        }
        return false
    }

    fun getAllUsers(): List<User> {
        return users.toList()
    }

    // פונקציות להתחברות והתנתקות
    fun login(userName: String, password: String): Boolean {
        val user = users.find { it.userName == userName && it.password == password }
        if (user != null) {
            currentUser = user
            return true
        }
        return false
    }

    fun logout() {
        currentUser = null
    }

    fun getCurrentUser(): User? {
        return currentUser
    }

    fun getUserByEmail(email: String): User? {
        return users.find { it.Email == email }
    }

    fun getUserByUsername(username: String): User? {
        return users.find { it.userName == username }
    }


    companion object {
        @Volatile
        private var instance: UsersManager? = null

        fun getInstance(): UsersManager {
            return instance ?: synchronized(this) {
                instance ?: UsersManager().also { instance = it }
            }
        }
    }
}