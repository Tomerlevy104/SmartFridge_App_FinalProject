package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class StartingPageActivity : AppCompatActivity() {

    private lateinit var startingPage_BTN_login: MaterialButton
    private lateinit var startingPage_BTN_register: MaterialButton
    private val userHandler = UserHandler.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Checks if there is a user logged in before displaying the screen
        if (userHandler.isUserLoggedIn()) {
            startMainActivity() //Go straight to the home screen
            return
        }

        // If no user is logged in - continues to Starting Page
        setContentView(R.layout.activity_starting_page)
        findViews()
        initViews()
    }

    private fun initViews() {

        //Login button
        startingPage_BTN_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        //Register button
        startingPage_BTN_register.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // מונע חזרה למסך זה עם כפתור Back
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun findViews() {
        startingPage_BTN_login = findViewById(R.id.startingPage_BTN_login)
        startingPage_BTN_register = findViewById(R.id.startingPage_BTN_register)
    }
}