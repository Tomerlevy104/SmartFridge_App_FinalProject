package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.InventoryAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.interfaces.IProductRepository
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.utilities.Constants

class InventoryListFragment : Fragment() {
    private lateinit var adapter: InventoryAdapter
    private var products = mutableListOf<Product>()
    private lateinit var inventory_list_BTN_categories: AppCompatButton
    private lateinit var inventory_list_BTN_all: AppCompatButton
    private lateinit var productRepository: IProductRepository
    private var selectedCategory: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCategory = arguments?.getString("SELECTED_CATEGORY")
        productRepository = ProductRepository()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        setupRecyclerView(view)
        setupClickListeners()
        loadProducts()
    }

    private fun loadProducts() {
        products.clear()

        // אם נבחרה קטגוריה, טען רק את המוצרים מאותה קטגוריה
        if (selectedCategory != null) {
            products.addAll(productRepository.getProductsByCategory(selectedCategory!!))
        } else {
            // אחרת, טען את כל המוצרים
            products.addAll(productRepository.getInitialProducts())
        }

        adapter.notifyDataSetChanged()
    }

    private fun findViews(view: View) {
        inventory_list_BTN_categories = view.findViewById(R.id.inventory_list_BTN_categories)
        inventory_list_BTN_all = view.findViewById(R.id.inventory_list_BTN_all)
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.inventory_list_RV_products)
        adapter = InventoryAdapter(
            products = products,
            onQuantityChanged = { product, newQuantity ->
                product.quantity = newQuantity
                adapter.notifyDataSetChanged()
            },
            onRemoveClicked = { product ->
                products.remove(product)
                adapter.notifyDataSetChanged()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun setupClickListeners() {
        // כפתור הקטגוריות - חזרה למסך הקטגוריות
        inventory_list_BTN_categories.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Activities.HOMEPAGE)
        }

        // כפתור להצגת כל המוצרים
        inventory_list_BTN_all.setOnClickListener {
            selectedCategory = null
            loadProducts()
        }
    }
}