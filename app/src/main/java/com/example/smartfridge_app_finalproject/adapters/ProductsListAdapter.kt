package com.example.smartfridge_app_finalproject.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.google.android.material.textview.MaterialTextView

class ProductsListAdapter(
    private var products: List<Product>,
    private val onQuantityChanged: (Product, Int) -> Unit,
    private val onRemoveClicked: (Product) -> Unit
) : RecyclerView.Adapter<ProductsListAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageProduct: AppCompatImageView = itemView.findViewById(R.id.products_list_IMG_product)
        private val tvName: MaterialTextView = itemView.findViewById(R.id.inventory_list_TV_name)
        private val tvQuantity: MaterialTextView = itemView.findViewById(R.id.inventory_list_TV_quantity)
        private val tvDate: MaterialTextView = itemView.findViewById(R.id.inventory_list_TV_date)
        private val btnIncrease: AppCompatImageView = itemView.findViewById(R.id.inventory_list_BTN_increase)
        private val btnDecrease: AppCompatImageView = itemView.findViewById(R.id.inventory_list_BTN_decrease)
        private val tvRemove: TextView = itemView.findViewById(R.id.inventory_list_TV_remove)

        //This method will insert the data into the views (name, quantity, date, image, etc.).
        fun bind(product: Product) {
            tvName.text = product.name
            tvQuantity.text = product.quantity.toString()
            tvDate.text = product.expiryDate


            // Load image using your preferred image loading library

            // Example with Glide:
            // Glide.with(itemView.context).load(product.imageUrl).into(imageProduct)

            if (product.imageUrl != 0) {  //Check if there is a picture
                imageProduct.setImageResource(product.imageUrl)
            } else {
                //If photo not exist - default picture
                imageProduct.setImageResource(R.drawable.category_no_picture)
            }

            //Increase product amount
            btnIncrease.setOnClickListener {
                onQuantityChanged(product, product.quantity + 1)
                //פה קורה לפונקציה שמעלה את כמות המוצר
            }

            //Decrease product amount
            btnDecrease.setOnClickListener {
                if (product.quantity > 0) {
                    onQuantityChanged(product, product.quantity - 1)
                }
            }

            //Delete product from inventory
            tvRemove.setOnClickListener {
                onRemoveClicked(product)
            }
        }
    }

    //This method is responsible for creating the ViewHolder.
    //which is an object that holds the view of a single item in the RecyclerView.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    //This method is responsible for connecting the data to the display items.
    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}