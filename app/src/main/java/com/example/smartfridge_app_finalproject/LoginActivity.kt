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
        if(FirebaseAuth.getInstance().currentUser==null){
            signIn()
        }
        else{
            transactToNextScreen()
        }

        findViews()
        //initViews()
    }

    // See: https://developer.android.com/training/basics/intents/result
    //Unsynchronize action, I send information and sometime I will receive information back
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }



    private fun signIn(){
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )

        // Create and launch sign-in intent
        //Here I can edit the login Page
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.drawable.app_logo_no_background)
            .build()
        signInLauncher.launch(signInIntent)
    }


    private fun transactToNextScreen(){
        intent = Intent(this,MainActivity::class.java)
        startActivity(intent)
        finish()
    }


    //When the sign-in flow is complete, you will receive the result in onSignInResult
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            transactToNextScreen()
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            Toast.makeText(this,"ההתחברות נכשלה",Toast.LENGTH_LONG).show()
            signIn()
        }
    }

    private fun signOut(){
        AuthUI.getInstance()
            .signOut(this)
            .addOnCompleteListener {
                // ...
            }
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