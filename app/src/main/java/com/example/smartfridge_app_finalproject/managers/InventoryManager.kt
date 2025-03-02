package com.example.smartfridge_app_finalproject.managers

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.ProductsListAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InventoryManager : IInventoryManager {
    private val productRepository = ProductRepository.getInstance()
    private val productManager = ProductManager.getInstance()
    private var products = mutableListOf<Product>()
    private var LocalProductsListInInventory = mutableListOf<Product>() //Products List

    private lateinit var productsListAdapter: ProductsListAdapter //Adapter

    private val userHandler = UserHandler.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()


//    override fun getProductsByCategory(category: String): List<Product> {
//        return products.filter { it.category == category }
//    }


    private fun getProductsByCategory(userId: String, category: String) {
        val productsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("products")
            .whereEqualTo("category", category)

        productsRef.get()
            .addOnSuccessListener { documents ->
                processProductDocuments(documents)
            }
            .addOnFailureListener { exception ->
                Log.e("ProductsList", "Error getting products", exception)
                Toast.makeText(requireContext(), "שגיאה בטעינת המוצרים", Toast.LENGTH_SHORT).show()
            }
    }



////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun addProduct(product: Product, onComplete: (Result<Unit>) -> Unit) {

        val currentUser =userHandler.getCurrentFirebaseUser()
        if(currentUser==null){
            onComplete(Result.failure(Exception("משתמש לא מחובר")))
            return
        }

        // Check if the product already exists
        firestore.collection("users")
            .document(currentUser.uid)
            .collection("products")
            .document(product.barCode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    onComplete(Result.failure(Exception("מוצר עם ברקוד זה כבר קיים")))
                } else {
                    // If the product doesn't exist, add it
                    firestore.collection("users")
                        .document(currentUser.uid)
                        .collection("products")
                        .document(product.barCode)
                        .set(product)
                        .addOnSuccessListener {
                            products.add(product) //Adding to local memory
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
///////////////////////////////////////////////////////////////////////////////////////////////////////////

    //Processes the results obtained from the query and updates the list
    private fun processProductDocuments(documents: QuerySnapshot) {
        LocalProductsListInInventory.clear()
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
                LocalProductsListInInventory.add(product)
            } catch (e: Exception) {
                Log.e("ProductsList", "Error converting document to Product", e)
                continue
            }
        }
        productsListAdapter.notifyDataSetChanged()
    }

    //אני צריך לשנות את הפונקציה הזאת כך שתעבוד עם הפייר בייס נראלי.
    override fun findProductsByName(name: String): List<Product> {
        return products.filter {
            it.name.contains(name, ignoreCase = true)
        }
    }

    override fun findProductByExactName(name: String): Product? {
        return products.find {
            it.name.equals(name, ignoreCase = true)
        }
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun clearCache() {
        products.clear()
    }
}