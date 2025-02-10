package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.widget.AppCompatEditText
import com.example.smartfridge_app_finalproject.data.model.User
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var register_TIL_firstname: TextInputLayout
    private lateinit var register_TIL_lastname: TextInputLayout
    private lateinit var register_TIL_email: TextInputLayout
    private lateinit var register_TIL_username: TextInputLayout
    private lateinit var register_TIL_password: TextInputLayout
    private lateinit var register_TIL_confirmpassword: TextInputLayout

    private lateinit var register_ET_firstname: AppCompatEditText
    private lateinit var register_ET_lastname: AppCompatEditText
    private lateinit var register_ET_email: AppCompatEditText
    private lateinit var register_ET_username: AppCompatEditText
    private lateinit var register_ET_password: AppCompatEditText
    private lateinit var register_ET_confirmpassword: AppCompatEditText

    private lateinit var register_BTN_submit: MaterialButton

    private val validInputManager = ValidInputManager.getInstance() //Valid input manager instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        findViews()
        initViews()
    }

    private fun findViews() {

        //TextInputLayouts
        register_TIL_firstname = findViewById(R.id.register_TIL_firstname)
        register_TIL_lastname = findViewById(R.id.register_TIL_lastname)
        register_TIL_email = findViewById(R.id.register_TIL_email)
        register_TIL_username = findViewById(R.id.register_TIL_username)
        register_TIL_password = findViewById(R.id.register_TIL_password)
        register_TIL_confirmpassword = findViewById(R.id.register_TIL_confirmpassword)

        //EditTexts
        register_ET_firstname = findViewById(R.id.register_ET_firstname)
        register_ET_lastname = findViewById(R.id.register_ET_lastname)
        register_ET_email = findViewById(R.id.register_ET_email)
        register_ET_username = findViewById(R.id.register_ET_username)
        register_ET_password = findViewById(R.id.register_ET_password)
        register_ET_confirmpassword = findViewById(R.id.register_ET_confirmpassword)

        // Button
        register_BTN_submit = findViewById(R.id.register_BTN_submit)
    }

    private fun initViews() {
        setupTextChangeListeners()

        register_BTN_submit.setOnClickListener {
            if (validateForm()) {
                handleRegistration()
            }
        }
    }

    private fun setupTextChangeListeners() {
        //Using a map of EditText to TextInputLayout for cleaner code
        val fieldPairs = mapOf(
            register_ET_firstname to register_TIL_firstname,
            register_ET_lastname to register_TIL_lastname,
            register_ET_email to register_TIL_email,
            register_ET_username to register_TIL_username,
            register_ET_password to register_TIL_password,
            register_ET_confirmpassword to register_TIL_confirmpassword
        )

        fieldPairs.forEach { (editText, inputLayout) ->
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) inputLayout.error = null
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val firstName = register_ET_firstname.text.toString().trim()
        val lastName = register_ET_lastname.text.toString().trim()
        val email = register_ET_email.text.toString().trim()
        val username = register_ET_username.text.toString().trim()
        val password = register_ET_password.text.toString()
        val confirmPassword = register_ET_confirmpassword.text.toString()

        // First Name validation
        if (!validInputManager.isValidFirstName(firstName)) {
            register_TIL_firstname.error = getString(R.string.invalid_first_name)
            isValid = false
        }

        // Last Name validation
        if (!validInputManager.isValidLastName(lastName)) {
            register_TIL_lastname.error = getString(R.string.invalid_last_name)
            isValid = false
        }

        // Email validation
        if (!validInputManager.isValidEmail(email)) {
            register_TIL_email.error = getString(R.string.invalid_email)
            isValid = false
        }

        // Username validation
        if (!validInputManager.isValidUsername(username)) {
            register_TIL_username.error = getString(R.string.invalid_user_name)
            isValid = false
        }

        // Password validation
        if (!validInputManager.isValidPassword(password)) {
            register_TIL_password.error = getString(R.string.password_must_contain_at_least)
            isValid = false
        }

        // Confirm password validation
        if (password != confirmPassword) {
            register_TIL_confirmpassword.error = getString(R.string.mismatched_passwords)
            isValid = false
        }

        return isValid
    }

    private fun handleRegistration() {
        val firstName = register_ET_firstname.text.toString().trim()
        val lastName = register_ET_lastname.text.toString().trim()
        val email = register_ET_email.text.toString().trim()
        val username = register_ET_username.text.toString().trim()
        val password = register_ET_password.text.toString()

        // Create Firebase Auth user
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser = FirebaseAuth.getInstance().currentUser

                    val user = User(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        userName = username,
                        password = "",  //I don't save the password in the firestore
                        profileImageUrl = "",
                        uid = firebaseUser?.uid ?: ""
                    )

                    //Add user to Firestore
                    FirebaseFirestore.getInstance().collection("users")
                        .document(firebaseUser?.uid ?: "")
                        .set(user.toMap())
                        .addOnSuccessListener {
                            showToast(getString(R.string.registration_success))
                            //Navigate to main activity
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            showToast(getString(R.string.registration_failed))
                        }
                } else {
                    showToast(getString(R.string.user_name_already_exist))
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}