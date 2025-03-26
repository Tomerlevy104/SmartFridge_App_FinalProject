package com.example.smartfridge_app_finalproject.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Service class for managing the product repository in firestore
 * * This service handles:
 *  - Adding products to the repository collection
 *  - Fetching all products from the repository
 *  - Getting product details by barcode
 */
class ProductRepositoryService {
    private val productsRepositoryDB = FirebaseFirestore.getInstance()

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Add a Product to Repository Of Products Collection
    fun addProductToRepository(
        product: Product,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        productsRepositoryDB.collection("RepositoryOfProducts")
            .document(product.barCode)
            .set(product)
            .addOnSuccessListener {
                Log.d(
                    "RepositoryService",
                    "The product with the barcode${product.barCode}Added successfully"
                )
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("RepositoryService", "Error adding product with barcode${product.barCode}", e)
                onFailure(e)
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get all products from the Repository Of Products Collection
    fun getAllProductsFromRepository(onSuccess: (List<Product>) -> Unit) {
        productsRepositoryDB.collection("RepositoryOfProducts")
            .get()
            .addOnSuccessListener { querySnapshot ->
                val repositoryProductsList = mutableListOf<Product>()

                for (document in querySnapshot.documents) {
                    try {
                        val barCode = document.id
                        val name = document.getString("name") ?: continue
                        val category = document.getString("category") ?: continue
                        val imageUrlString = document.getString("imageUrl")
                        val imageUrl =
                            if (!imageUrlString.isNullOrEmpty()) Uri.parse(imageUrlString) else Uri.EMPTY

                        // Create a product with default quantity of 1 and empty expiration date
                        val product = Product(
                            barCode = barCode,
                            name = name,
                            category = category,
                            imageUrl = imageUrl,
                            quantity = 1,
                            expiryDate = "" // Empty
                        )
                        repositoryProductsList.add(product)
                    } catch (e: Exception) {
                        Log.e(
                            "ProductRepositoryService",
                            "Error parsing product document: ${e.message}"
                        )
                    }
                }

                onSuccess(repositoryProductsList)
            }
            .addOnFailureListener { exception ->
                Log.e("ProductRepositoryService", "Error getting products: ${exception.message}")
                onSuccess(emptyList()) // Return empty list on failure
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Get a Product object from repository by barcode
    fun getProductByBarcode(barcode: String, onComplete: (Product?) -> Unit) {
        productsRepositoryDB.collection("RepositoryOfProducts")
            .document(barcode)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    try {
                        val name = document.getString("name") ?: ""
                        val category = document.getString("category") ?: ""
                        val imageUrl = document.getString("imageUrl")

                        // Create expiry date (2 weeks from now)
                        val expiryDate = LocalDate.now().plusWeeks(2)
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

                        // Create Product object - with default quantity of 1
                        val product = Product(
                            barCode = barcode,
                            name = name,
                            category = category,
                            imageUrl = imageUrl?.let { Uri.parse(it) } ?: Uri.EMPTY,
                            quantity = 1, // Default quantity
                            expiryDate = expiryDate
                        )

                        onComplete(product)
                    } catch (e: Exception) {
                        Log.e("Repository", "Error creating product object: ${e.message}")
                        onComplete(null)
                    }
                } else {
                    // Product not found
                    onComplete(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Repository", "Error getting product: ${exception.message}")
                onComplete(null)
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Retrieves products from the repository that belong to a specific category
    fun getProductsByCategory(category: String, onSuccess: (List<Product>) -> Unit) {
        productsRepositoryDB.collection("RepositoryOfProducts")
            .whereEqualTo("category", category)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val products = querySnapshot.toObjects(Product::class.java)
                onSuccess(products)
            }
            .addOnFailureListener {
                onSuccess(emptyList())
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Creates and populates the initial product repository in firestore
    fun createProductsRepository(context: Context) {
        val db = FirebaseFirestore.getInstance()
        val productsCollection = db.collection("RepositoryOfProducts")

        val sampleProducts = listOf(
            // Fruits
            Product(
                barCode = "7290012163933",
                name = context.getString(R.string.apple),
                category = Constants.Category.FRUITS_AND_VEGETABLES,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fgolden_apple.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290010807012",
                name = context.getString(R.string.tomatos),
                category = Constants.Category.FRUITS_AND_VEGETABLES,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Ftomatoes.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Drinks
            Product(
                barCode = "7290008341387",
                name = context.getString(R.string.coca_cola_1_5_liters),
                category = Constants.Category.DRINKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fcola.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290010372157",
                name = context.getString(R.string.water_neviot),
                category = Constants.Category.DRINKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fwater.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Meat and fish
            Product(
                barCode = "7290102419701",
                name = context.getString(R.string.chicken),
                category = Constants.Category.MEAT,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fchicken_breast.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290110703111",
                name = context.getString(R.string.fresh_salmon),
                category = Constants.Category.MEAT,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fsalmon.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Milk
            Product(
                barCode = "7290004131074",
                name = context.getString(R.string.tnuva_milk_3_precent),
                category = Constants.Category.DAIRY,
                imageUrl = Uri.parse("gs://smartfridgeapp-40e65.firebasestorage.app/products_images/Milk.jpg"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290000066318",
                name = context.getString(R.string.cotege_5_precent_tnuva),
                category = Constants.Category.DAIRY,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fcottage.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Breads and baked goods
            Product(
                barCode = "7290110089888",
                name = context.getString(R.string.engel_white_bread),
                category = Constants.Category.BREADS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fwhite_bread.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290108880014",
                name = context.getString(R.string.lahmaniot),
                category = Constants.Category.BREADS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Frolls.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Snacks and sweets
            Product(
                barCode = "7290106578463",
                name = context.getString(R.string.milk_chocolate),
                category = Constants.Category.SNACKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fchocolate.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290111073019",
                name = context.getString(R.string.bisli_onion),
                category = Constants.Category.SNACKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fbisli.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // Freeze
            Product(
                barCode = "7290109805005",
                name = context.getString(R.string.freeze_pizza),
                category = Constants.Category.FROZEN,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Ffrozen_pizza.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290100005486",
                name = context.getString(R.string.vanilla_ice_cream),
                category = Constants.Category.FROZEN,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fvanilla_ice_cream.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            )
        )

        // Adding products to the collection in batch
        val batch = db.batch()
        sampleProducts.forEach { product ->
            val docRef = productsCollection.document(product.barCode)
            batch.set(docRef, product)
        }
        // Execute the batch
        batch.commit()
            .addOnSuccessListener {
                Log.d(
                    "Repository",
                    context.getString(
                        R.string.successfully_created_repository_with_products,
                        sampleProducts.size
                    )
                )
            }
            .addOnFailureListener { e ->
                Log.e(
                    "Repository",
                    context.getString(R.string.error_creating_RepositoryOfProducts_collection), e
                )
            }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////
}