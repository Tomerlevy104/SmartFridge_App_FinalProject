package com.example.smartfridge_app_finalproject.adapters

import android.net.Uri
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
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        // This method will insert the data into the views (name, quantity, date, image, etc.)
        fun bind(product: Product) {
            tvName.text = product.name
            tvQuantity.text = product.quantity.toString()
            tvDate.text = product.expiryDate

            //Load product image using Glide
            if (product.imageUrl != Uri.EMPTY) {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.category_no_picture)
                    .error(R.drawable.category_no_picture)
                    .into(imageProduct)
            } else {
                imageProduct.setImageResource(R.drawable.category_no_picture)
            }

            // Increase product amount
            btnIncrease.setOnClickListener {
                val newQuantity = product.quantity + 1
                updateProductQuantity(product, newQuantity)
            }

            // Decrease product amount
            btnDecrease.setOnClickListener {
                if (product.quantity > 0) {
                    val newQuantity = product.quantity - 1
                    updateProductQuantity(product, newQuantity)
                }
            }

            // Delete product from inventory
            tvRemove.setOnClickListener {
                removeProduct(product)
            }
        }

        private fun updateProductQuantity(product: Product, newQuantity: Int) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(itemView.context, "משתמש לא מחובר", Toast.LENGTH_SHORT).show()
                return
            }

            val db = FirebaseFirestore.getInstance()
            val productRef = db.collection("users")
                .document(currentUser.uid)
                .collection("products")
                .document(product.barCode)

            productRef.update("quantity", newQuantity)
                .addOnSuccessListener {
                    // Call the callback to update the UI
                    onQuantityChanged(product, newQuantity)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        itemView.context,
                        "שגיאה בעדכון הכמות: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        private fun removeProduct(product: Product) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                Toast.makeText(itemView.context, "משתמש לא מחובר", Toast.LENGTH_SHORT).show()
                return
            }

            val db = FirebaseFirestore.getInstance()
            val productRef = db.collection("users")
                .document(currentUser.uid)
                .collection("products")
                .document(product.barCode)

            productRef.delete()
                .addOnSuccessListener {
                    // Call the callback to update the UI
                    onRemoveClicked(product)
                    Toast.makeText(itemView.context, "המוצר הוסר בהצלחה", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        itemView.context,
                        "שגיאה במחיקת המוצר: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount() = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}