package com.example.smartfridge_app_finalproject.managers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepositoryService
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Inventory Manager - Responsible for the logic of managing products in inventory
 */
class InventoryManager : IInventoryManager {
    private val userHandler = UserHandlerManager.getInstance()
    private val currentUser = userHandler.getCurrentFirebaseUser()
    private val productRepositoryService = ProductRepositoryService()

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Loading all products in stock
    override fun loadAllProducts(callback: (List<Product>) -> Unit) {
        if (currentUser == null) {
            callback(emptyList())
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val productsList = parseProductsFromDocuments(documents)
                callback(productsList)
            }
            .addOnFailureListener { exception ->
                Log.e("InventoryManager", "Error loading products", exception)
                callback(emptyList())
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Loading products by category
    fun getProductsByCategory(
        userId: String,
        category: String,
        callback: (List<Product>) -> Unit
    ) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("products")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { documents ->
                val productsList = parseProductsFromDocuments(documents)
                callback(productsList)
            }
            .addOnFailureListener { exception ->
                Log.e("InventoryManager", "Error loading products from the category", exception)
                callback(emptyList())
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Checks if a product with the specified barcode exists in the user's inventory
    override fun productIsExistInUserInventory(barcode: String, onResult: (Product?) -> Unit) {
        if (currentUser == null) {
            onResult(null)
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(barcode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val product = Product(
                            barCode = document.getString("barCode") ?: "",
                            name = document.getString("name") ?: "",
                            category = document.getString("category") ?: "",
                            imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) }
                                ?: Uri.EMPTY,
                            quantity = document.getLong("quantity")?.toInt() ?: 0,
                            expiryDate = document.getString("expiryDate") ?: ""
                        )
                        onResult(product)
                    } catch (e: Exception) {
                        Log.e("InventoryManager", "Error converting document to Product", e)
                        onResult(null)
                    }
                } else {
                    onResult(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("InventoryManager", "Error checking product", exception)
                onResult(null)
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Check if product is exist in firestore collection
    fun productIsExistInRepositoryOfProductCollection(barcode: String, isExist: (Boolean) -> Unit) {
        // Check if product exists in RepositoryOfProduct collection
        FirebaseFirestore.getInstance()
            .collection("RepositoryOfProducts")
            .document(barcode)
            .get()
            .addOnSuccessListener { document ->
                // Return true if the document exists, false otherwise
                isExist(document.exists())
            }
            .addOnFailureListener { exception ->
                Log.d(
                    "InventoryManager",
                    "Error checking product in RepositoryOfProduct Collection: ${exception.message}",
                    exception
                )
                // Return false on failure
                isExist(false)
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add a new product to user's inventory
    override fun addNewProduct(product: Product, onComplete: (Result<Unit>) -> Unit) {
        if (currentUser == null) {
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }
        // Add the product directly to user's inventory
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .set(product)
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onComplete(Result.failure(exception))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add a product to user's inventory using data from Repository Of Products Collection
    private fun addProductFromRepository(
        barcode: String,
        onComplete: (Result<Unit>) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }
        //Getting the product details from Repository Of Products Collection
        productRepositoryService.getProductByBarcode(barcode) { product ->
            if (product != null) {
                // Add product to user's inventory using existing function
                addNewProduct(product, onComplete)
            } else {
                onComplete(Result.failure(Exception("מוצר לא נמצא במאגר")))
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Updates the quantity of a product in the user's inventory
    override fun updateProductQuantity(
        barcode: String,
        newQuantity: Int,
        onComplete: (Result<Unit>) -> Unit
    ) {
        if (currentUser == null) {
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }
        // Update in Firebase
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(barcode)
            .update("quantity", newQuantity)
            .addOnSuccessListener {
                onComplete(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onComplete(Result.failure(exception))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Checks if a product is expired based on its expiry date string.
    // @param expiryDateStr The expiry date in format "dd/MM/yyyy"
    // @return true if the product is expired, false otherwise
    fun isProductExpired(expiryDateStr: String): Boolean {
        try {
            // Make sure we're using the correct date format
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            // Get current date without time portion for fair comparison
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val currentDate = calendar.time

            // Parse the expiry date
            val expiryDate = dateFormat.parse(expiryDateStr) ?: return false

            // If expiry date is before the current date, the product is expired
            return expiryDate.before(currentDate)
        } catch (e: Exception) {
            // Log any parsing errors
            Log.e("InventoryManager", "Error parsing date: ${e.message}", e)
            return false
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Handle the process of adding a product to inventory
    //his function checks if the product exists in the user's inventory or repository of products collection
    //and takes appropriate action
    fun handleAddProduct(product: Product, onComplete: (Result<Unit>) -> Unit) {
        // Check if user is logged in
        if (currentUser == null) {
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }
        // First - check if product exists in user's inventory
        productIsExistInUserInventory(product.barCode) { existingProduct ->
            if (existingProduct != null) {
                // Product exists in user inventory, update quantity
                val newQuantity = existingProduct.quantity + product.quantity
                updateProductQuantity(product.barCode, newQuantity) { result ->
                    onComplete(result)
                }
            } else {
                // Second - product doesn't exist in user inventory, check if it exists in repository
                productIsExistInRepositoryOfProductCollection(product.barCode) { existsInRepository ->
                    if (existsInRepository) {
                        // Product exists in repository, add it from there
                        addNewProduct(product, onComplete)
                    } else {
                        // Product doesn't exist in repository, add it as new
                        addNewProduct(product, onComplete)
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Deleting a product from the database
    override fun removeProduct(product: Product, onComplete: ((Result<Unit>) -> Unit)) {
        if (currentUser == null) {
            onComplete.invoke(Result.failure(Exception("משתמש לא מחובר")))
            return
        }
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .delete()
            .addOnSuccessListener {
                // Creates an object that represents an operation that succeeded and does not return a specific value
                onComplete.invoke(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onComplete.invoke(Result.failure(exception))
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Search for products by search string
    override fun searchProducts(
        context: Context,
        searchQuery: String,
        categoryFilter: String?,
        callback: (List<Product>) -> Unit
    ) {
        if (searchQuery.isEmpty()) {
            Toast.makeText(context, R.string.enter_product_name, Toast.LENGTH_SHORT).show()
            callback(emptyList())
            return
        }
        if (currentUser == null) {
            Toast.makeText(context, R.string.no_user_loged_in, Toast.LENGTH_SHORT).show()
            callback(emptyList())
            return
        }
        val lowerCaseSearchString = searchQuery.lowercase()
        //Preparing the query - if there is a filter category, add it to the query later
        val query = FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
        //If there is a filter category, we will search only within the category
        val filteredQuery = if (categoryFilter != null) {
            query.whereEqualTo("category", categoryFilter)
        } else {
            query
        }
        filteredQuery.get().addOnSuccessListener { products ->
            val matchingProducts = mutableListOf<Product>()
            for (document in products) {
                try {
                    val productNameFromDoc = document.getString("name") ?: continue
                    if (productNameFromDoc.lowercase().contains(lowerCaseSearchString)) {
                        val product = Product(
                            barCode = document.getString("barCode") ?: continue,
                            name = productNameFromDoc,
                            category = document.getString("category") ?: continue,
                            imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) }
                                ?: Uri.EMPTY,
                            quantity = document.getLong("quantity")?.toInt() ?: continue,
                            expiryDate = document.getString("expiryDate") ?: continue
                        )
                        matchingProducts.add(product)
                    }
                } catch (e: Exception) {
                    Log.e("Inventory", "Error converting document to Product", e)
                }
            }
            if (matchingProducts.isNotEmpty()) {
                if (matchingProducts.size == 1) {
                    Toast.makeText(context, R.string.product_found, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, R.string.similar_products_found, Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                //Customized message in case there are no results and there is filtering by category
                if (categoryFilter != null) {
                    Toast.makeText(
                        context,
                        context.getString(
                            R.string.no_products_were_found_in_category_X_for_the_search_term,
                            categoryFilter,
                            searchQuery
                        ), Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(context, R.string.product_not_found, Toast.LENGTH_SHORT)
                        .show()
                }
            }
            callback(matchingProducts)
        }
            .addOnFailureListener { exception ->
                Log.e("InventoryManager", "שגיאה בחיפוש מוצרים", exception)
                Toast.makeText(
                    context,
                    "שגיאה בחיפוש: ${exception.message}",
                    Toast.LENGTH_SHORT
                )
                    .show()
                callback(emptyList())
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Converting query results from document to products
    private fun parseProductsFromDocuments(documents: QuerySnapshot): List<Product> {
        val productsList = mutableListOf<Product>()
        for (document in documents) {
            try {
                val product = Product(
                    barCode = document.getString("barCode") ?: continue,
                    name = document.getString("name") ?: continue,
                    category = document.getString("category") ?: continue,
                    imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) }
                        ?: Uri.EMPTY,
                    quantity = document.getLong("quantity")?.toInt() ?: continue,
                    expiryDate = document.getString("expiryDate") ?: continue
                )
                productsList.add(product)
            } catch (e: Exception) {
                Log.e("InventoryManager", "Error converting document to Product", e)
            }
        }
        return productsList
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
}