package com.example.smartfridge_app_finalproject.data.repository

import com.example.smartfridge_app_finalproject.data.model.User

class UsersRepository {

    fun getInitialUsers() = listOf(
        User(
            firstName = "תומר",
            lastName = "לוי",
            email = "tomer@gmail.com",
            userName = "tomer_l",
            password = "tomer123",
            profileImageUrl = null
        ),
        User(
            firstName = "איתי",
            lastName = "לוי",
            email = "itay@gmail.com",
            userName = "itay_l",
            password = "itay123",
            profileImageUrl = null
        ),
        User(
            firstName = "סיגלית",
            lastName = "לוי",
            email = "sigalit@gmail.com",
            userName = "sigalit_l",
            password = "sigalit123",
            profileImageUrl = null
        ),
        User(
            firstName = "בני",
            lastName = "לוי",
            email = "beni@gmail.com",
            userName = "benny_l",
            password = "benny123",
            profileImageUrl = null
        ),
        User(
            firstName = "דניאל",
            lastName = "לוי",
            email = "daniel@gmail.com",
            userName = "daniel_l",
            password = "daniel123",
            profileImageUrl = null
        )
    )
}