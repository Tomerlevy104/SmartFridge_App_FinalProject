package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InventoryManager : IInventoryManager {
    private val allProductsInInventory = ProductRepository()
    private val products = mutableListOf<Product>()

    init {
        // טעינת המוצרים ההתחלתיים לרשימה
        products.addAll(allProductsInInventory.getInitialProducts())
    }

    override fun getProductsByCategory(category: String): List<Product> {
        return products.filter { it.category == category }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return flowOf(products)
    }

    override fun addProduct(product: Product): Boolean {
        return try {
            //Check if the barcode is not exist
            if (products.any { it.barCode == product.barCode }) {
                return false
            }

            products.add(product)
            true
        } catch (e: Exception) {
            false
        }
    }

//    // פונקציית עזר לייצור ברקוד חדש
//    private fun generateNewBarcode(category: String): String {
//        val prefix = when(category) {
//            Constants.Category.FRUITS_AND_VEGETABLES -> "FV"
//            Constants.Category.DRINKS -> "DR"
//            Constants.Category.MEAT -> "MF"
//            Constants.Category.ORGANICANDHEALTH -> "OH"
//            Constants.Category.SNACKS -> "SN"
//            Constants.Category.BREADS -> "BR"
//            Constants.Category.CLEANING -> "CL"
//            Constants.Category.COOKINGBAKING -> "CB"
//            Constants.Category.ANIMALS -> "PT"
//            Constants.Category.DAIRY -> "DA"
//            Constants.Category.BABYS -> "BP"
//            Constants.Category.FROZEN -> "FR"
//            else -> "OT"
//        }
//
//        val existingCodes = products
//            .filter { it.barCode.startsWith(prefix) }
//            .map { it.barCode.substring(2).toIntOrNull() ?: 0 }
//
//        val nextNumber = (existingCodes.maxOrNull() ?: 0) + 1
//        return "$prefix${String.format("%03d", nextNumber)}"
//    }
}