package com.example.smartfridge_app_finalproject.managers

import android.net.Uri
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.google.firebase.storage.FirebaseStorage

class ProductManager private constructor() {

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    companion object {
        @Volatile
        private var instance: ProductManager? = null

        fun getInstance(): ProductManager {
            return instance ?: synchronized(this) {
                instance ?: ProductManager().also { instance = it }
            }
        }
    }

     fun uploadProductImage(userId: String, barCode: String, imageUri: Uri, onComplete: (Result<String>) -> Unit) {
        val imageRef = storage.reference.child("products/$userId/$barCode.jpg")
        imageRef.putFile(imageUri)
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                imageRef.downloadUrl
            }
            .addOnSuccessListener { uri ->
                onComplete(Result.success(uri.toString()))
            }
            .addOnFailureListener { exception ->
                onComplete(Result.failure(exception))
            }
    }
}