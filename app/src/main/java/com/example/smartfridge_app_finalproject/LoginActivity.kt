package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import com.example.smartfridge_app_finalproject.managers.UserHandlerManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Login page
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var login_ET_username: AppCompatEditText
    private lateinit var login_ET_password: AppCompatEditText
    private lateinit var login_BTN_forgot_password: MaterialButton
    private lateinit var login_BTN_login: MaterialButton
    private lateinit var login_BTN_for_register_press_here: MaterialButton
    private lateinit var login_BTN_back: MaterialButton
    private val userHandlerManager = UserHandlerManager.getInstance()

    private val auth = FirebaseAuth.getInstance()

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        findViews()
        initViews()
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun findViews() {
        login_ET_username = findViewById(R.id.login_ET_username)
        login_ET_password = findViewById(R.id.login_ET_password)
        login_BTN_forgot_password = findViewById(R.id.login_BTN_forgot_password)
        login_BTN_login = findViewById(R.id.login_BTN_login)
        login_BTN_for_register_press_here = findViewById(R.id.login_BTN_for_register_press_here)
        login_BTN_back = findViewById(R.id.login_BTN_back)

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initViews() {
        login_BTN_login.setOnClickListener {
            performLogin()
        }

        login_BTN_forgot_password.setOnClickListener {
            handleForgotPassword()
        }

        login_BTN_for_register_press_here.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        login_BTN_back.setOnClickListener {
            navigateToStartingPage()
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Navigate back to StartingPageActivity
    private fun navigateToStartingPage() {
        val intent = Intent(this, StartingPageActivity::class.java)
        startActivity(intent)
        finish() // Close current activity
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun performLogin() {
        val validInputManager = ValidInputManager.getInstance()
        val email = login_ET_username.text.toString().trim()
        val password = login_ET_password.text.toString()

        //Validate input
        // Email
        if (email.isEmpty()) {
            login_ET_username.error = getString(R.string.please_enter_an_email)
            return
        } else if (!validInputManager.isValidEmail(email)) {
            login_ET_username.error = getString(R.string.invalid_email)
            return
        }

        // Password
        if (password.isEmpty()) {
            login_ET_password.error = getString(R.string.please_enter_an_password)
            return
        } else if (!validInputManager.isValidPassword(password)) {
            login_ET_password.error = getString(R.string.password_must_contain_at_least)
            return
        }

        //Attempt login with Firebase
        userHandlerManager.loginUser(email, password) { result ->
            result.onSuccess { user ->
                // After successful login, get user information
                userHandlerManager.getUserData(user.uid) { dataResult ->
                    dataResult.onSuccess {
                        // Go to the main screen
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }.onFailure { _ ->
                        showToast(getString(R.string.no_user_information_found))
                    }
                }
            }.onFailure { _ ->
                showToast(getString(R.string.login_failed))
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun handleForgotPassword() {
        val email = login_ET_username.text.toString().trim()

        if (email.isEmpty()) {
            login_ET_username.error =
                getString(R.string.please_enter_an_email_to_restore_your_password)
            return
        }

        userHandlerManager.sendPasswordResetEmail(email) { result ->
            result.onSuccess {
                showToast(getString(R.string.password_reset_instructions_sent_to_your_email))
            }.onFailure {
                showToast(getString(R.string.error_sending_password_reset_email))
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////////////
}