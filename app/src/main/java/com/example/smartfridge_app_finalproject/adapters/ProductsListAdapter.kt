package com.example.smartfridge_app_finalproject.adapters

import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.google.android.material.textview.MaterialTextView
import java.util.Locale

/**
 * Adapter for displaying product items in a RecyclerView with interactive controls
 * This adapter manages the display of products in the user's inventory, providing
 * functionality to view product details, update quantities, and remove items.
 * It handles image loading with Glide, expiration date checking, and user interactions
 * for inventory management
 */
class ProductsListAdapter(
    private var products: List<Product>,
    private val onQuantityChanged: (Product, Int) -> Unit,
    private val onRemoveClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductsListAdapter.ProductViewHolder>() {

    private val inventoryManager = InventoryManager()

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: AppCompatImageView =
            itemView.findViewById(R.id.products_list_IMG_product)
        private val tvName: MaterialTextView = itemView.findViewById(R.id.inventory_list_TV_name)
        private val tvQuantity: MaterialTextView =
            itemView.findViewById(R.id.inventory_list_TV_quantity)
        private val tvDate: MaterialTextView = itemView.findViewById(R.id.inventory_list_TV_date)
        private val tvExpiry: MaterialTextView =
            itemView.findViewById(R.id.inventory_list_TV_expiry)
        private val tvExpiryLabel: MaterialTextView =
            itemView.findViewById(R.id.inventory_list_TV_expiry_lbl)
        private val btnIncrease: AppCompatImageView =
            itemView.findViewById(R.id.inventory_list_BTN_increase)
        private val btnDecrease: AppCompatImageView =
            itemView.findViewById(R.id.inventory_list_BTN_decrease)
        private val tvRemove: TextView = itemView.findViewById(R.id.inventory_list_TV_remove)

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        // This method will populate the views with data (name, quantity, date, image, etc.)
        fun bind(product: Product) {
            tvName.text = product.name
            tvQuantity.text = String.format(Locale.getDefault(), "%d", product.quantity)
            tvDate.text = product.expiryDate
            tvExpiry.text = itemView.context.getString(R.string.expiration_date)

            // Check if the product is expired
            checkIfExpired(product.expiryDate)

            // Load product image using Glide
            if (product.imageUrl != Uri.EMPTY) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.category_no_picture)
                    .error(R.drawable.category_no_picture)
                    .into(imageProduct)
            } else {
                imageProduct.setImageResource(R.drawable.category_no_picture)
            }

            // Increase product quantity
            btnIncrease.setOnClickListener {
                val newQuantity = product.quantity + 1
                updateProductQuantity(product, newQuantity)
            }

            // Decrease product quantity
            btnDecrease.setOnClickListener {
                if (product.quantity > 0) {
                    val newQuantity = product.quantity - 1
                    updateProductQuantity(product, newQuantity)
                }
            }

            // Remove product from inventory
            tvRemove.setOnClickListener {
                removeProduct(product)
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Check if a product is expired and update the expiry label visibility
        private fun checkIfExpired(expiryDateStr: String) {
            try {
                // Expiration check
                val isExpired = inventoryManager.isProductExpired(expiryDateStr)

                // Update UI based on expiration status
                if (isExpired) {
                    Log.d("ProductsListAdapter", "Product is EXPIRED")
                    tvExpiryLabel.visibility = View.VISIBLE
                } else {
                    Log.d("ProductsListAdapter", "Product is NOT expired")
                    tvExpiryLabel.visibility = View.INVISIBLE
                }
            } catch (e: Exception) {
                // Log any errors
                Log.e("ProductsListAdapter", "Error checking expiry date: ${e.message}", e)
                tvExpiryLabel.visibility = View.INVISIBLE
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Update product quantity
        private fun updateProductQuantity(product: Product, newQuantity: Int) {
            inventoryManager.updateProductQuantity(product.barCode, newQuantity) { result ->
                result.onSuccess {
                    onQuantityChanged(product, newQuantity) // Call callback to update UI
                }
            }
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        // Remove product
        private fun removeProduct(product: Product) {
            inventoryManager.removeProduct(product) { result ->
                result.onSuccess {
                    onRemoveClicked(product) // Call callback to update UI
                    // Show success message
                    Toast.makeText(itemView.context, "המוצר הוסר בהצלחה!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getItemCount() = products.size
}