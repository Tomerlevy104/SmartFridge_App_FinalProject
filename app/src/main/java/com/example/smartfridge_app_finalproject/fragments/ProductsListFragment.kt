package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.ProductsListAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.textview.MaterialTextView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class ProductsListFragment : Fragment() {
    private lateinit var productsListAdapter: ProductsListAdapter //Adapter
    private var theProductsListInInventory = mutableListOf<Product>() //Products List
    private val inventoryManager: IInventoryManager = InventoryManager() //Inventory manager
    private lateinit var inventory_list_BTN_categories: AppCompatButton
    private lateinit var products_list_TV_categoryName: MaterialTextView
    private lateinit var products_list_IMG_category: AppCompatImageView
    private var selectedCategory: String? = null
    private var selectedCategoryImage: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCategory = arguments?.getString("SELECTED_CATEGORY")
        selectedCategoryImage = arguments?.getInt("SELECTED_CATEGORY_IMAGE")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        setupRecyclerView(view)
        setupClickListeners()
        loadProducts()
    }

    private fun findViews(view: View) {
        inventory_list_BTN_categories = view.findViewById(R.id.inventory_list_BTN_categories)
        products_list_TV_categoryName = view.findViewById(R.id.products_list_TV_categoryName)
        products_list_IMG_category = view.findViewById(R.id.products_list_IMG_category)
    }

    private fun loadProducts() {
        theProductsListInInventory.clear()
        products_list_TV_categoryName.text = selectedCategory //Set tittel of chosen category
        selectedCategoryImage?.let { //Set image of chosen category
            products_list_IMG_category.setImageResource(it)
        }

        //If category selected - show all products in this category
        if (selectedCategory != null) {
            theProductsListInInventory.addAll(
                inventoryManager.getProductsByCategory(
                    selectedCategory!!
                )
            )
        } else {
            //Else - show all products
            runBlocking {
                theProductsListInInventory.addAll(inventoryManager.getAllProducts().first())
            }
        }
        productsListAdapter.notifyDataSetChanged()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.inventory_list_RV_products)
        productsListAdapter = ProductsListAdapter(
            products = theProductsListInInventory,
            onQuantityChanged = { product, newQuantity ->
                product.quantity = newQuantity
                productsListAdapter.notifyDataSetChanged()
            },
            onRemoveClicked = { product ->
                theProductsListInInventory.remove(product)
                productsListAdapter.notifyDataSetChanged()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = productsListAdapter
    }

    private fun setupClickListeners() {
        //Categories button click
        inventory_list_BTN_categories.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.HOMEPAGE)
        }
    }
}