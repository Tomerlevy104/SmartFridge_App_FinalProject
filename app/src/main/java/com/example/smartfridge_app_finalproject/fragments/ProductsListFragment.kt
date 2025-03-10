package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.ProductsListAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView

//Fragment to display the list of products in stock
class ProductsListFragment : Fragment() {

    private lateinit var productsListAdapter: ProductsListAdapter
    private val productsList = mutableListOf<Product>() //List of products to display
    private val inventoryManager = InventoryManager() //Inventory manager
    private val userHandler = UserHandler.getInstance()

    //UI Components
    private lateinit var productsListBtnCategories: AppCompatButton
    private lateinit var productsListBtnSearch: MaterialButton
    private lateinit var productsListEtSearch: AppCompatEditText
    private lateinit var productsListTvCategoryName: MaterialTextView
    private lateinit var productsListImgCategory: ShapeableImageView
    private lateinit var productsListImgProfile: ShapeableImageView
    private lateinit var productsListTvName: MaterialTextView
    private lateinit var productsListIvLocation: AppCompatImageButton
    private lateinit var productsListRvProducts: RecyclerView
    private var selectedCategory: String? = null
    private var selectedCategoryImage: Int? = null
    private var fromSearch: Boolean = false
    private var searchQuery: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Receive the parameters passed to the fragment
        arguments?.let {
            selectedCategory = it.getString("SELECTED_CATEGORY")
            if (it.containsKey("SELECTED_CATEGORY_IMAGE")) {
                selectedCategoryImage = it.getInt("SELECTED_CATEGORY_IMAGE", -1)
                if (selectedCategoryImage == -1) selectedCategoryImage = null
            }
            fromSearch = it.getBoolean("FROM_SEARCH", false)
            searchQuery = it.getString("SEARCH_QUERY")
        }
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
        setupClickListeners()
        setupRecyclerView()
        updateUI() //Update the UI with the details of the selected category or search
        loadInitialProducts() //Loading the appropriate products
        loadUserProfile() //Load user details
    }

    private fun findViews(view: View) {
        productsListBtnCategories = view.findViewById(R.id.products_list_BTN_categories)
        productsListTvCategoryName = view.findViewById(R.id.products_list_TV_categoryName)
        productsListImgCategory = view.findViewById(R.id.products_list_IMG_category)
        productsListBtnSearch = view.findViewById(R.id.products_list_BTN_search)
        productsListEtSearch = view.findViewById(R.id.products_list_ET_search)
        productsListImgProfile = view.findViewById(R.id.products_list_IMG_profile)
        productsListTvName = view.findViewById(R.id.products_list_TV_name)
        productsListIvLocation = view.findViewById(R.id.products_list_IV_location)
        productsListRvProducts = view.findViewById(R.id.products_list_RV_products)
        //If coming from a search, I will fill the edit text in the search field
        if (fromSearch && !searchQuery.isNullOrEmpty()) {
            productsListEtSearch.setText(searchQuery)
        }
    }

    private fun setupClickListeners() {
        //All categories button
        productsListBtnCategories.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.HOMEPAGE)
        }
        //Search button
        productsListBtnSearch.setOnClickListener {
            executeSearch()
        }
        //Map button
        productsListIvLocation.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.SUPERMARKET)
        }
    }

    private fun setupRecyclerView() {
        productsListAdapter = ProductsListAdapter(
            products = productsList,
            onQuantityChanged = { product, newQuantity ->
                //Update product quantity in firestore
                inventoryManager.updateProductQuantity(product, newQuantity) { result ->
                    result.onSuccess {
                        //Update the local list after a successful update
                        val index = productsList.indexOfFirst { it.barCode == product.barCode }
                        if (index != -1) {
                            productsList[index] = product.copy(quantity = newQuantity)
                            activity?.runOnUiThread {
                                productsListAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            },
            onRemoveClicked = { product ->
                //Deleting the product from firestore
                inventoryManager.removeProduct(product) { result ->
                    result.onSuccess {
                        //Removing the product from the local list after a successful deletion
                        productsList.removeAll { it.barCode == product.barCode }
                        activity?.runOnUiThread {
                            productsListAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        )

        productsListRvProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productsListAdapter
        }
    }

    //Loading the initial products - according to the parameters received
    private fun loadInitialProducts() {
        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.no_user_loged_in), Toast.LENGTH_SHORT
            ).show()
            return
        }

        //If coming from a search, perform a search
        if (fromSearch && !searchQuery.isNullOrEmpty()) {
            inventoryManager.searchProducts(
                requireContext(),
                searchQuery!!,
                null
            ) { searchResults ->
                productsList.clear()
                productsList.addAll(searchResults)
                activity?.runOnUiThread {
                    productsListAdapter.notifyDataSetChanged()
                    Log.d(
                        "ProductsListFragment",
                        "Loaded ${searchResults.size} products from search"
                    )
                }
            }
        }
        //Else if there is a selected category, only load the products from that category
        else if (selectedCategory != null) {
            inventoryManager.getProductsByCategory(
                currentUser.uid,
                selectedCategory!!
            ) { categoryProducts ->
                productsList.clear()
                productsList.addAll(categoryProducts)
                activity?.runOnUiThread {
                    productsListAdapter.notifyDataSetChanged()
                }
            }
        }
        //Otherwise, load all products
        else {
            inventoryManager.loadAllProducts { allProducts ->
                productsList.clear()
                productsList.addAll(allProducts)
                activity?.runOnUiThread {
                    productsListAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    //Update user interface with the details of the selected category or search results
    private fun updateUI() {
        //If coming from a search
        if (fromSearch && !searchQuery.isNullOrEmpty()) {
            productsListTvCategoryName.text = "תוצאות חיפוש: $searchQuery"
            productsListImgCategory.visibility = View.GONE
        }
        //If coming from a category selection
        else {
            productsListTvCategoryName.text = selectedCategory ?: getString(R.string.inventory_btn)
            if (selectedCategoryImage != null) {
                productsListImgCategory.visibility = View.VISIBLE
                productsListImgCategory.setImageResource(selectedCategoryImage!!)
            } else {
                productsListImgCategory.visibility = View.GONE
            }
        }
    }

    //Perform a search
    private fun executeSearch() {
        val searchQuery = productsListEtSearch.text.toString().trim()

        if (searchQuery.isNotEmpty()) {
            //Search for products with filtering by category if there is a filtering category
            inventoryManager.searchProducts(
                requireContext(),
                searchQuery,
                selectedCategory
            ) { results ->
                fromSearch = true //Update the flag that we are now in search results
                this.searchQuery = searchQuery

                productsList.clear()
                productsList.addAll(results) //Update list
                updateUI() //Update title according to search
                productsListAdapter.notifyDataSetChanged()
            }
        }
        //If the search field is empty, load all products again
        else {
            fromSearch = false
            this.searchQuery = null
            loadInitialProducts()
            updateUI()
        }
    }

    //Loading user details
    private fun loadUserProfile() {
        if (userHandler.isUserLoggedIn()) {
            userHandler.getCurrentUserData()?.let { userData ->
                updateUIWithUserData(userData)
            }
            //Load/refresh data from Firestore
            userHandler.loadUserProfile { result ->
                result.onSuccess { userData ->
                    activity?.runOnUiThread {
                        updateUIWithUserData(userData)
                    }
                }.onFailure { exception ->
                    activity?.runOnUiThread {
                        productsListTvName.text = getString(R.string.guest)
                        Log.e("ProductsListFragment", "Error loading profile", exception)
                    }
                }
            }
        } else {
            productsListTvName.text = getString(R.string.guest)
        }
    }

    //Update UI with user information
    private fun updateUIWithUserData(userData: UserHandler.UserData) {
        productsListTvName.text = userData.firstName

        //Load the profile picture
        if (!userData.profileImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(userData.profileImageUrl)
                .placeholder(R.drawable.profile_man)
                .error(R.drawable.profile_man)
                .into(productsListImgProfile)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile()

        //Refresh data only if it doesn't come from a search
        if (!fromSearch) {
            loadInitialProducts()
            updateUI()
        }
    }
}