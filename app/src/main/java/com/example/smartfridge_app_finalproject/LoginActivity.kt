package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var login_ET_username: AppCompatEditText
    private lateinit var login_ET_password: AppCompatEditText
    private lateinit var login_BTN_forgot_password: MaterialButton
    private lateinit var login_BTN_login: MaterialButton
    private lateinit var login_BTN_for_register_press_here: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViews()
        //initViews()
    }

























    private fun initViews() {
        TODO("Not yet implemented")
    }

    private fun findViews() {
        login_ET_username = findViewById(R.id.login_ET_username)
        login_ET_password = findViewById(R.id.login_ET_password)
        login_BTN_forgot_password = findViewById(R.id.login_BTN_forgot_password)
        login_BTN_login = findViewById(R.id.login_BTN_login)
        login_BTN_for_register_press_here = findViewById(R.id.login_BTN_for_register_press_here)
    }
}