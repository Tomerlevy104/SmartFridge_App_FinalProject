package com.example.smartfridge_app_finalproject

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton

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
        // מכניס את תוכן דף ההתחברות לתוך ה-contentContainer
        //val loginContent = layoutInflater.inflate(R.layout.activity_login, null)
        //findViewById<FrameLayout>(R.id.contentContainer).addView(loginContent)

        // מסתיר את סרגל הניווט התחתון בדף ההתחברות
        //findViewById<BottomNavigationView>(R.id.bottomNavigation).visibility = View.GONE

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