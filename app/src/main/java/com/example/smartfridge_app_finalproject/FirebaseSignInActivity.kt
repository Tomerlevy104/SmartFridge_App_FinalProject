package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseSignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_firebase_sign_in)
        if(FirebaseAuth.getInstance().currentUser==null){
            signIn()
        }
        else{
            transactToNextScreen()
        }
    }

    // See: https://developer.android.com/training/basics/intents/result
    //Unsynchronize action, I send information and sometime I will receive information back
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
    ) { res ->
        this.onSignInResult(res)
    }

    private fun transactToNextScreen(){
        intent = Intent(this,RegisterActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun signIn() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.PhoneBuilder().build(),
        )

        val customLayout = AuthMethodPickerLayout.Builder(R.layout.custom_auth_layout)
            .setEmailButtonId(R.id.email_button)
            .setPhoneButtonId(R.id.phone_button)
            .build()

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false) // מבטל את Smart Lock כדי למנוע בעיות
            .setAuthMethodPickerLayout(customLayout)
            .build()

        signInLauncher.launch(signInIntent)
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
            Toast.makeText(this,"ההתחברות נכשלה", Toast.LENGTH_LONG).show()
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
}