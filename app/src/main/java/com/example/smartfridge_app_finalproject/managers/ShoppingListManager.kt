package com.example.smartfridge_app_finalproject.managers

import android.net.Uri
import android.util.Log
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.interfaces.IShoppingListManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Manager class for handling shopping list operations
 */
class ShoppingListManager : IShoppingListManager {
    private val TAG = "ShoppingListManager"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()


    //Get the current user ID or null if not authenticated
    private val currentUserId: String?
        get() = auth.currentUser?.uid

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Get all shopping list items for the current user
    override fun getAllItems(onComplete: (List<Product>) -> Unit) {
        val userId = currentUserId
        if (userId == null) {
            onComplete(emptyList())
            return
        }

        firestore.collection("users")
            .document(userId)
            .collection("shoppingList")
            .get()
            .addOnSuccessListener { documents ->
                val items = documents.mapNotNull { doc ->
                    try {
                        val product = Product(
                            barCode = doc.id,  // Using document ID as barcode
                            name = doc.getString("productName") ?: return@mapNotNull null,
                            category = doc.getString("category") ?: "",
                            imageUrl = doc.getString("imageUrl")?.let { Uri.parse(it) }
                                ?: Uri.EMPTY,
                            quantity = doc.getLong("quantity")?.toInt() ?: 1,
                            expiryDate = doc.getString("expiryDate") ?: ""
                        )
                        product
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing shopping list item", e)
                        null
                    }
                }
                onComplete(items)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting shopping list items", e)
                onComplete(emptyList())
            }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Add a new shopping list item
    override fun addItem(product: Product, onComplete: (Boolean, String?) -> Unit) {
        val userId = currentUserId
        if (userId == null) {
            Log.e(TAG, "Error adding shopping list item: currentUserId is null")
            onComplete(false, "המשתמש אינו מחובר")
            return
        }
        Log.d(TAG, "Adding item to shopping list for user: $userId")
        Log.d(TAG, "Product details - Name: ${product.name}, Barcode: ${product.barCode}")
        val item = hashMapOf(
            "productName" to product.name,
            "category" to product.category,
            "imageUrl" to product.imageUrl.toString(),
            "quantity" to product.quantity,
            "expiryDate" to product.expiryDate,
            "addedAt" to com.google.firebase.Timestamp.now()
        )
        try {
            // Use product barcode as the document ID
            firestore.collection("users")
                .document(userId)
                .collection("shoppingList")
                .document(product.barCode)
                .set(item)
                .addOnSuccessListener {
                    Log.d(TAG, "Successfully added item to shopping list: ${product.barCode}")
                    onComplete(true, null)
                }
                .addOnFailureListener { e ->
                    val errorMsg = "Error adding shopping list item: ${e.message}"
                    Log.e(TAG, errorMsg, e)
                    onComplete(false, errorMsg)
                }
        } catch (e: Exception) {
            val errorMsg = "Exception trying to add shopping list item: ${e.message}"
            Log.e(TAG, errorMsg, e)
            onComplete(false, errorMsg)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    //Remove a shopping list item
    override fun removeItem(barcode: String, onComplete: (Boolean) -> Unit) {
        val userId = currentUserId
        if (userId == null) {
            onComplete(false)
            return
        }
        firestore.collection("users")
            .document(userId)
            .collection("shoppingList")
            .document(barcode)
            .delete()
            .addOnSuccessListener {
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing shopping list item", e)
                onComplete(false)
            }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}