package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository

class ProductManager private constructor() {
    private val productRepository = ProductRepository()
    private val inventory = mutableMapOf<String, Product>()

    init {
        // Initialize inventory with products from repository
        productRepository.getInitialProducts().forEach { product ->
            inventory[product.barCode] = product
        }
    }


//    override fun removeProduct(barCode: String): Boolean { // למחוק נראלי
//
//    }
//
//    override fun updateProductQuantity(barCode: String, newQuantity: Int): Boolean {
//        if (newQuantity < 0) return false
//
//        return try {
//
//        }
//
//    }
//
//    override fun getProductByBarcode(barCode: String): Product? {
//        return inventory[barCode]
//    }
//
//
//    companion object {
//        @Volatile
//        private var instance: ProductManager? = null
//
//        fun getInstance(): ProductManager {
//            return instance ?: synchronized(this) {
//                instance ?: ProductManager().also { instance = it }
//            }
//        }
//    }
}