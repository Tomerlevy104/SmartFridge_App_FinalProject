package com.example.smartfridge_app_finalproject.data.repository

import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.User

class UsersRepository {

    // רשימה התחלתית של משתמשים לטעון מהפייר בייס?
    fun getInitialUsers() = listOf(
        User(
            firstName = "תומר",
            lastName = "לוי",
            Email = "tomer@gmail.com",
            userName = "tomer_l",
            password = "tomer123",
            imageResourceId = R.drawable.profile_man
        ),
        User(
            firstName = "איתי",
            lastName = "לוי",
            Email = "itay@gmail.com",
            userName = "itay_l",
            password = "itay123",
            imageResourceId = R.drawable.profile_man
        ),
        User(
            firstName = "סיגלית",
            lastName = "לוי",
            Email = "sigalit@gmail.com",
            userName = "sigalit_l",
            password = "sigalit123",
            imageResourceId = R.drawable.profile_man
        ),
        User(
            firstName = "בני",
            lastName = "לוי",
            Email = "beni@gmail.com",
            userName = "benny_l",
            password = "benny123",
            imageResourceId = R.drawable.profile_man
        ),
        User(
            firstName = "דניאל",
            lastName = "לוי",
            Email = "daniel@gmail.com",
            userName = "daniel_l",
            password = "daniel123",
            imageResourceId = R.drawable.profile_man
        )
    )

}