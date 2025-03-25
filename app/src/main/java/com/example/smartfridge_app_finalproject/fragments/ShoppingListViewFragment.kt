package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.managers.ShoppingListManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textview.MaterialTextView

class ShoppingListViewFragment : Fragment() {

    // UI components
    private lateinit var scrollViewItems: androidx.appcompat.widget.LinearLayoutCompat
    private lateinit var statusTextView: MaterialTextView
    private lateinit var emptyStateTextView: MaterialTextView

    // Shopping list manager
    private val shoppingListManager = ShoppingListManager()

    // Data
    private var shoppingItems = mutableListOf<Product>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_shopping_list_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findViews(view)
        loadShoppingList()
    }

    private fun findViews(view: View) {
        scrollViewItems = view.findViewById(R.id.shopping_list_view_LL_items)
        statusTextView = view.findViewById(R.id.shopping_list_view_TV_status)
        emptyStateTextView = view.findViewById(R.id.shopping_list_view_TV_empty)
    }

    private fun loadShoppingList() {
        shoppingListManager.getAllItems { items ->
            activity?.runOnUiThread {
                shoppingItems = items.toMutableList()
                updateStatusText()
                renderItems()
            }
        }
    }

    private fun renderItems() {
        // Clear existing items
        scrollViewItems.removeAllViews()

        if (shoppingItems.isEmpty()) {
            emptyStateTextView.visibility = View.VISIBLE
        } else {
            emptyStateTextView.visibility = View.GONE

            // Add each item
            for (item in shoppingItems) {
                val itemView = createItemView(item)
                scrollViewItems.addView(itemView)
            }
        }
    }

    private fun createItemView(product: Product): View {
        val itemView = layoutInflater.inflate(R.layout.item_shopping_list_view, null)

        val productImageView: AppCompatImageView = itemView.findViewById(R.id.shopping_item_view_IMG_product)
        val productNameTextView: MaterialTextView = itemView.findViewById(R.id.shopping_item_view_TV_name)
        val completedCheckBox: MaterialCheckBox = itemView.findViewById(R.id.shopping_item_view_CHKB_completed)

        // Set product name
        productNameTextView.text = product.name

        // Set product image
        if (product.imageUrl != android.net.Uri.EMPTY) {
            Glide.with(requireContext())
                .load(product.imageUrl)
                .placeholder(R.drawable.category_no_picture)
                .error(R.drawable.category_no_picture)
                .into(productImageView)
        } else {
            productImageView.setImageResource(R.drawable.category_no_picture)
        }

        // Set checkbox to unchecked initially
        completedCheckBox.isChecked = false

        // Set checkbox click listener to remove the item when checked
        completedCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Remove the item from the database using the barcode
                shoppingListManager.removeItem(product.barCode) { success ->
                    if (success) {
                        activity?.runOnUiThread {
                            // Remove the item from our local list
                            shoppingItems.remove(product)
                            // Re-render the list
                            renderItems()
                            // Update status text
                            updateStatusText()

                            Toast.makeText(
                                requireContext(),
                                "המוצר ${product.name} הוסר מהרשימה",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        activity?.runOnUiThread {
                            // If removal failed, uncheck the checkbox
                            completedCheckBox.isChecked = false

                            Toast.makeText(
                                requireContext(),
                                "שגיאה בהסרת המוצר",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        return itemView
    }

    private fun updateStatusText() {
        val totalItems = shoppingItems.size
        statusTextView.text = "פריטים ברשימת הקניות: $totalItems"
    }

    override fun onResume() {
        super.onResume()
        // Refresh the list when returning to this fragment
        loadShoppingList()
    }
}