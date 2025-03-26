package com.example.smartfridge_app_finalproject.data.model

import android.net.Uri

/**
 * Product model
 */
data class Product(val barCode: String,
                   val name: String,
                   val category: String,
                   var imageUrl: Uri,
                   var quantity: Int,
                   val expiryDate: String)
