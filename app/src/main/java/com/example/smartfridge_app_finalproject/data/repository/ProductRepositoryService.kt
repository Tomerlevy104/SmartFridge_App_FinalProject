package com.example.smartfridge_app_finalproject.data.repository

import android.net.Uri
import android.util.Log
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProductRepositoryService {
    private val productsRepositoryDB = FirebaseFirestore.getInstance()

    /**
     * Add a Product to Repository Of Products Collection
     */
    fun addProductToRepository(product: Product, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        productsRepositoryDB.collection("RepositoryOfProducts")
            .document(product.barCode)
            .set(product)
            .addOnSuccessListener {
                Log.d("RepositoryService", "The product with the barcode${product.barCode}Added successfully")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("RepositoryService", "Error adding product with barcode${product.barCode}", e)
                onFailure(e)
            }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Get all products from the Repository Of Products Collection
     */
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
                        val imageUrl = if (!imageUrlString.isNullOrEmpty()) Uri.parse(imageUrlString) else Uri.EMPTY

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
                        Log.e("ProductRepositoryService", "Error parsing product document: ${e.message}")
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
    /**
     * Get a Product object from repository by barcode
     */
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
    fun createProductsRepository() {
        val db = FirebaseFirestore.getInstance()
        val productsCollection = db.collection("RepositoryOfProducts")

        val sampleProducts = listOf(
            // פירות וירקות
            Product(
                barCode = "7290012163933",
                name = "תפוח עץ זהוב",
                category = Constants.Category.FRUITS_AND_VEGETABLES,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fgolden_apple.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290010807012",
                name = "עגבניות",
                category = Constants.Category.FRUITS_AND_VEGETABLES,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Ftomatoes.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // משקאות
            Product(
                barCode = "7290008341387",
                name = "קוקה קולה 1.5 ליטר",
                category = Constants.Category.DRINKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fcola.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290010372157",
                name = "מים מינרליים נביעות",
                category = Constants.Category.DRINKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fwater.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // בשר ודגים
            Product(
                barCode = "7290102419701",
                name = "חזה עוף טרי",
                category = Constants.Category.MEAT,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fchicken_breast.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290110703111",
                name = "סלמון טרי",
                category = Constants.Category.MEAT,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fsalmon.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // מוצרי חלב
            Product(
                barCode = "7290004131074",
                name = "חלב תנובה 3%",
                category = Constants.Category.DAIRY,
                imageUrl = Uri.parse("gs://smartfridgeapp-40e65.firebasestorage.app/products_images/Milk.jpg"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290000066318",
                name = "קוטג' תנובה 5%",
                category = Constants.Category.DAIRY,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fcottage.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // לחמים ומוצרי מאפה
            Product(
                barCode = "7290110089888",
                name = "לחם לבן של אנג'ל",
                category = Constants.Category.BREADS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fwhite_bread.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290108880014",
                name = "לחמניות המאפייה",
                category = Constants.Category.BREADS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Frolls.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // חטיפים ומתוקים
            Product(
                barCode = "7290106578463",
                name = "שוקולד פרה חלב",
                category = Constants.Category.SNACKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fchocolate.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290111073019",
                name = "ביסלי בטעם בצל",
                category = Constants.Category.SNACKS,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fbisli.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),

            // קפואים
            Product(
                barCode = "7290109805005",
                name = "פיצה קפואה",
                category = Constants.Category.FROZEN,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Ffrozen_pizza.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            ),
            Product(
                barCode = "7290100005486",
                name = "גלידה וניל",
                category = Constants.Category.FROZEN,
                imageUrl = Uri.parse("https://firebasestorage.googleapis.com/v0/b/smartfridgeapp-40e65.appspot.com/o/repository%2Fvanilla_ice_cream.jpg?alt=media"),
                quantity = 0,
                expiryDate = ""
            )
        )

        // הוספת המוצרים לאוסף ב-batch
        val batch = db.batch()

        sampleProducts.forEach { product ->
            val docRef = productsCollection.document(product.barCode)
            batch.set(docRef, product)
        }

        // ביצוע ה-batch
        batch.commit()
            .addOnSuccessListener {
                Log.d(
                    "Repository",
                    "נוצר בהצלחה אוסף RepositoryOfProducts עם ${sampleProducts.size} מוצרים"
                )
            }
            .addOnFailureListener { e ->
                Log.e("Repository", "שגיאה ביצירת אוסף RepositoryOfProducts", e)
            }
    }
    /////////////////////////////////////////////////////////////////////////////////////////////////////
}