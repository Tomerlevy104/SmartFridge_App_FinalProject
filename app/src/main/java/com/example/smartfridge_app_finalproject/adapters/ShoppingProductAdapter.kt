package com.example.smartfridge_app_finalproject.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.google.android.material.textview.MaterialTextView

/**
 * Adapter for displaying products in the shopping list with an option to add them to the cart
 */
class ShoppingProductAdapter(
    private var products: List<Product>,
    private val onAddToCartClicked: (Product) -> Unit
) : RecyclerView.Adapter<ShoppingProductAdapter.ShoppingProductViewHolder>() {

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    inner class ShoppingProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: AppCompatImageView =
            itemView.findViewById(R.id.shopping_item_IMG_product)
        private val tvName: MaterialTextView = itemView.findViewById(R.id.shopping_item_TV_name)
        private val imgAddToCart: AppCompatImageView =
            itemView.findViewById(R.id.shopping_item_IMG_addToCart)

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////
        fun bind(product: Product) {
            // Set product name
            tvName.text = product.name

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

            // Set click listener for the add to cart button
            imgAddToCart.setOnClickListener {
                onAddToCartClicked(product)
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_product, parent, false)
        return ShoppingProductViewHolder(view)
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onBindViewHolder(holder: ShoppingProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun getItemCount() = products.size
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////
}