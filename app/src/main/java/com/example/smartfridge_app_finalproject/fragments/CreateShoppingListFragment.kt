package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.ShoppingProductAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepositoryService
import com.example.smartfridge_app_finalproject.managers.ShoppingListManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.button.MaterialButton

/**
 * Fragment to display the list of products for adding to the shopping list
 */
class CreateShoppingListFragment : Fragment() {

    // UI Components
    private lateinit var shoppingListRvProducts: RecyclerView
    private lateinit var shoppingListBtnViewCart: AppCompatButton
    private lateinit var shoppingListBtnSearch: MaterialButton
    private lateinit var shoppingListEtSearch: AppCompatEditText

    // Adapter
    private lateinit var shoppingProductAdapter: ShoppingProductAdapter

    // Data
    private var productsList = mutableListOf<Product>()

    // Services
    private val productRepositoryService = ProductRepositoryService()
    private val shoppingListManager = ShoppingListManager()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_shopping_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        setupRecyclerView()
        setupClickListeners()
        loadProducts()
    }

    private fun findViews(view: View) {
        shoppingListRvProducts = view.findViewById(R.id.shopping_list_RV_products)
        shoppingListBtnViewCart = view.findViewById(R.id.shopping_list_BTN_viewCart)
        shoppingListBtnSearch = view.findViewById(R.id.shopping_list_BTN_search)
        shoppingListEtSearch = view.findViewById(R.id.shopping_list_ET_search)
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list
        shoppingProductAdapter = ShoppingProductAdapter(
            products = productsList,
            onAddToCartClicked = { product ->
                // Add product to shopping list using ShoppingListManager
                shoppingListManager.addItem(product) { success, errorMsg ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                getString(
                                    R.string.product_added_successfuly_to_shopping_list,
                                    product.name
                                ),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            // Show detailed error message
                            val message = getString(
                                R.string.error_adding_product_to_shopping_list,
                                errorMsg ?: getString(R.string.unknown_reason)
                            )
                            Toast.makeText(
                                requireContext(),
                                message,
                                Toast.LENGTH_LONG
                            ).show()

                            // Log the error
                            Log.d("CreateShoppingList", message)
                        }
                    }
                }
            }
        )

        // Set up the RecyclerView
        shoppingListRvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = shoppingProductAdapter
        }
    }

    private fun setupClickListeners() {
        // View shopping list button
        shoppingListBtnViewCart.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.SHOPPINGLIST)
        }

        // Search button
        shoppingListBtnSearch.setOnClickListener {
            executeSearch()
        }
    }

    /**
     * Load all products from repository
     */
    private fun loadProducts() {
        productRepositoryService.getAllProductsFromRepository { products ->
            productsList.clear()
            productsList.addAll(products)
            activity?.runOnUiThread {
                shoppingProductAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun executeSearch() {
        val searchQuery = shoppingListEtSearch.text.toString().trim()

        if (searchQuery.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "נא להזין מילת חיפוש",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val filteredProducts = productsList.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        if (filteredProducts.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "לא נמצאו מוצרים התואמים לחיפוש",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            productsList.clear()
            productsList.addAll(filteredProducts)
            shoppingProductAdapter.notifyDataSetChanged()
        }
    }
}