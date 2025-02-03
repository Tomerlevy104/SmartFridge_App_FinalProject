package com.example.smartfridge_app_finalproject.managers

import android.content.Context
import android.widget.Toast
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InventoryManager : IInventoryManager {
    private val productRepository = ProductRepository()
    private val products = mutableListOf<Product>()

    init {
        products.addAll(productRepository.getInitialProducts())
    }

    override fun getProductsByCategory(category: String): List<Product> {
        return products.filter { it.category == category }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return flowOf(products.toList())
    }

    override fun addProduct(product: Product): Boolean {
        return try {
            //Check if the barcode already exist
            if (products.any { it.barCode == product.barCode }) {
                return false
            }
            products.add(product)
            true
        } catch (e: Exception) {
            false
        }
    }

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

    private fun formatProductDetails(context: Context, product: Product): String {
        return """
            ${context.getString(R.string.name)}: ${product.name}
            ${context.getString(R.string.category)}: ${product.category}
            ${context.getString(R.string.quantity)}: ${product.quantity}
            ${context.getString(R.string.expiration_date)}: ${product.expiryDate}
            ${"-".repeat(30)}
        """.trimIndent()
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showProductFoundDialog(context: Context, product: Product) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.product_found)
            .setMessage(formatProductDetails(context, product))
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showSimilarProductsDialog(context: Context, products: List<Product>) {
        val productsText = products.joinToString("\n\n") { product ->
            formatProductDetails(context, product)
        }

        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.similar_products_found)
            .setMessage(productsText)
            .setPositiveButton(context.getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun searchProduct(context: Context, productName: String) {
        if (productName.isEmpty()) {
            showToast(context, context.getString(R.string.enter_product_name))
            return
        }

        //First try exact match
        val exactProduct = findProductByExactName(productName)
        if (exactProduct != null) {
            showProductFoundDialog(context, exactProduct)
            return
        }

        //If no exact match, try partial matches
        val similarProducts = findProductsByName(productName)
        if (similarProducts.isNotEmpty()) {
            showSimilarProductsDialog(context, similarProducts)
        } else {
            showToast(context, context.getString(R.string.product_not_found))
        }
    }
}