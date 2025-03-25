package com.example.smartfridge_app_finalproject.interfaces

import android.content.Context
import com.example.smartfridge_app_finalproject.data.model.Product

//An interface that defines the actions that can be performed on product inventory
interface IInventoryManager {

    fun getProductsByCategory(userId: String, category: String, callback: (List<Product>) -> Unit)
//    fun addProduct(product: Product, onComplete: (Result<Unit>) -> Unit)
    fun searchProducts(context: Context, searchQuery: String, categoryFilter: String? = null, callback: (List<Product>) -> Unit)
}