package com.example.smartfridge_app_finalproject.interfaces

import com.example.smartfridge_app_finalproject.data.model.Product
import kotlinx.coroutines.flow.Flow

interface IProductRepository {
    fun getInitialProducts(): List<Product>
    fun getProductsByCategory(category: String): List<Product>

//     fun addProduct(product: Product)
//     fun getAllProducts(): Flow<List<Product>>
//     fun updateProductAmount(product: Product)
//     fun deleteProduct(productId: String)
//     fun getExpiringProducts(): Flow<List<Product>>

}

