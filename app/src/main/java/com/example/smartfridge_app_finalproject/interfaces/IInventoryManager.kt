package com.example.smartfridge_app_finalproject.interfaces

import android.content.Context
import com.example.smartfridge_app_finalproject.data.model.Product
import kotlinx.coroutines.flow.Flow

interface IInventoryManager {

    fun getAllProducts(): Flow<List<Product>>
    fun getProductsByCategory(category: String): List<Product>
    fun addProduct(product: Product): Boolean
    fun findProductsByName(name: String): List<Product>
    fun findProductByExactName(name: String): Product?
    fun searchProduct(context: Context, productName: String)


    //fun getInitialProducts(): List<Product>
//    fun addNewProduct(product: Product): Boolean
//    fun removeProduct(barCode: String): Boolean
//    fun updateProductQuantity(barCode: String, newQuantity: Int): Boolean
//    fun getProductByBarcode(barCode: String): Product?
//    fun getAllProducts(): List<Product>
//     fun addProduct(product: Product)
//     fun updateProductAmount(product: Product)
//     fun deleteProduct(productId: String)
//     fun getExpiringProducts(): Flow<List<Product>>

}

