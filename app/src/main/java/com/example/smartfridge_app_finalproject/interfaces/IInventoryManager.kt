package com.example.smartfridge_app_finalproject.interfaces

import android.content.Context
import com.example.smartfridge_app_finalproject.data.model.Product

interface IInventoryManager {

    /**
     * Loads all products from the user's inventory
     * @param callback Function that receives the list of products when loading completes
     */
    fun loadAllProducts(callback: (List<Product>) -> Unit)

    /**
     * Checks if a product with the specified barcode exists in the user's inventory
     * @param barcode The product barcode to check
     * @param onResult Callback that receives the product if found, or null if not found
     */
    fun productIsExistInUserInventory(barcode: String, onResult: (Product?) -> Unit)

    /**
     * Adds a new product to the user's inventory
     * @param product The product to add
     * @param onComplete Callback with the result of the operation (success or failure)
     */
    fun addNewProduct(product: Product, onComplete: (Result<Unit>) -> Unit)

    /**
     * Updates the quantity of a product in the user's inventory
     * @param barcode The barcode of the product to update
     * @param newQuantity The new quantity value
     * @param onComplete Callback with the result of the operation (success or failure)
     */
    fun updateProductQuantity(barcode: String, newQuantity: Int, onComplete: (Result<Unit>) -> Unit)

    /**
     * Removes a product from the user's inventory
     * @param product The product to remove
     * @param onComplete Optional callback with the result of the operation (success or failure)
     */
    fun removeProduct(product: Product, onComplete: ((Result<Unit>) -> Unit))

    /**
     * Searches for products in the user's inventory based on a search query
     * @param context Context used for displaying toast messages
     * @param searchQuery The search string to match against product names
     * @param categoryFilter Optional category to filter results
     * @param callback Function that receives the list of matching products
     */
    fun searchProducts(context: Context, searchQuery: String, categoryFilter: String? = null, callback: (List<Product>) -> Unit)
}