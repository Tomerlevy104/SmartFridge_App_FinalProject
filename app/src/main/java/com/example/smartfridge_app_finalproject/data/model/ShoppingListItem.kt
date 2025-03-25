package com.example.smartfridge_app_finalproject.data.model

import android.net.Uri

/**
 * Data model for a shopping list item
 */
data class ShoppingListItem(
    val id: String,
    val productName: String,
    val imageUrl: Uri = Uri.EMPTY,
    var isCompleted: Boolean = false,
    val productBarcode: String? = null
)