package com.example.smartfridge_app_finalproject.interfaces

import com.example.smartfridge_app_finalproject.data.model.Product

interface IShoppingListManager {

    /**
     * Get all shopping list items for the current user
     */
    fun getAllItems(onComplete: (List<Product>) -> Unit)

    /**
     * Add a new shopping list item
     */
    fun addItem(product: Product, onComplete: (Boolean, String?) -> Unit)

    /**
     * Remove a shopping list item
     */
    fun removeItem(barcode: String, onComplete: (Boolean) -> Unit)
}