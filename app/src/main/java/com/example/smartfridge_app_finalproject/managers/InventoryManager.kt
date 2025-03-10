package com.example.smartfridge_app_finalproject.managers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

//Inventory Manager - Responsible for the logic of managing products in inventory
class InventoryManager : IInventoryManager {
    private val userHandler = UserHandler.getInstance()

    //Loading all products in stock
    fun loadAllProducts(callback: (List<Product>) -> Unit) {
        val currentUser = userHandler.getCurrentFirebaseUser()
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

    //Loading products by category
    override fun getProductsByCategory(userId: String, category: String, callback: (List<Product>) -> Unit) {
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

    //Adding a product to the database
    override fun addProduct(product: Product, onComplete: (Result<Unit>) -> Unit) {
        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }

        //Check if the product already exists
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    //Existing product - updates quantity
                    val existingProduct = document.toObject(Product::class.java)
                    if (existingProduct != null) {
                        updateProductQuantity(existingProduct, existingProduct.quantity + product.quantity, onComplete)
                    } else {
                        onComplete(Result.failure(Exception("מוצר קיים אך לא ניתן לקרוא את הנתונים שלו")))
                    }
                } else {
                    //New product - adds
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
            }
            .addOnFailureListener { exception ->
                onComplete(Result.failure(exception))
            }
    }

    //Update quantity of an existing product
    fun updateProductQuantity(product: Product, newQuantity: Int, onComplete: ((Result<Unit>) -> Unit)? = null) {
        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            onComplete?.invoke(Result.failure(Exception("משתמש לא מחובר")))
            return
        }

        //Update in Firebase
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .update("quantity", newQuantity)
            .addOnSuccessListener {
                onComplete?.invoke(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onComplete?.invoke(Result.failure(exception))
            }
    }

    //Deleting a product from the database
    fun removeProduct(product: Product, onComplete: ((Result<Unit>) -> Unit)? = null) {
        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            onComplete?.invoke(Result.failure(Exception("משתמש לא מחובר")))
            return
        }

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .delete()
            .addOnSuccessListener {
                onComplete?.invoke(Result.success(Unit))
            }
            .addOnFailureListener { exception ->
                onComplete?.invoke(Result.failure(exception))
            }
    }

    //Search for products by search string
    override fun searchProducts(context: Context, searchQuery: String, categoryFilter: String?, callback: (List<Product>) -> Unit) {
        if (searchQuery.isEmpty()) {
            Toast.makeText(context, R.string.enter_product_name, Toast.LENGTH_SHORT).show()
            callback(emptyList())
            return
        }

        val currentUser = userHandler.getCurrentFirebaseUser()
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
                                imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) } ?: Uri.EMPTY,
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
                        Toast.makeText(context, R.string.similar_products_found, Toast.LENGTH_SHORT).show()
                    }
                } else {
                    //Customized message in case there are no results and there is filtering by category
                    if (categoryFilter != null) {
                        Toast.makeText(context, "לא נמצאו מוצרים בקטגוריה '$categoryFilter' למילת החיפוש '$searchQuery'", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, R.string.product_not_found, Toast.LENGTH_SHORT).show()
                    }
                }

                callback(matchingProducts)
            }
            .addOnFailureListener { exception ->
                Log.e("InventoryManager", "שגיאה בחיפוש מוצרים", exception)
                Toast.makeText(context, "שגיאה בחיפוש: ${exception.message}", Toast.LENGTH_SHORT).show()
                callback(emptyList())
            }
    }

    //Converting query results from document to products
    private fun parseProductsFromDocuments(documents: QuerySnapshot): List<Product> {
        val productsList = mutableListOf<Product>()

        for (document in documents) {
            try {
                val product = Product(
                    barCode = document.getString("barCode") ?: continue,
                    name = document.getString("name") ?: continue,
                    category = document.getString("category") ?: continue,
                    imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) } ?: Uri.EMPTY,
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
}