package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.CategoryAdapter
import com.example.smartfridge_app_finalproject.data.model.Category
import com.example.smartfridge_app_finalproject.managers.CategoryManager
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.bumptech.glide.Glide
import android.util.Log
import android.widget.Toast

class HomePageFragment : Fragment() {

    private lateinit var categoryManager: CategoryManager
    private var inventoryManager: InventoryManager = InventoryManager()
    private val categoriesList = MutableLiveData<List<Category>>()
    private val userHandlerManager = UserHandlerManager.getInstance()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var homePage_RV_categories: RecyclerView
    private lateinit var homepage_BTN_show_all: AppCompatButton
    private lateinit var homepage_ET_search: AppCompatEditText
    private lateinit var homepage_BTN_search: MaterialButton
    private lateinit var homepage_IMG_profile: ShapeableImageView
    private lateinit var homepage_TV_name: MaterialTextView

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryManager = CategoryManager(activity as MainActivity)
        inventoryManager = InventoryManager()
        findViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadCategories()
        observeCategories()
        loadUserProfile()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun findViews(view: View) {
        homePage_RV_categories = view.findViewById(R.id.homePage_RV_categories)
        homepage_BTN_show_all = view.findViewById(R.id.homepage_BTN_show_all)
        homepage_ET_search = view.findViewById(R.id.products_list_ET_search)
        homepage_BTN_search = view.findViewById(R.id.homepage_BTN_search)
        homepage_IMG_profile = view.findViewById(R.id.homepage_IMG_profile)
        homepage_TV_name = view.findViewById(R.id.homepage_TV_name)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupClickListeners() {
        //Show all button
        homepage_BTN_show_all.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
        }
        //Search button
        homepage_BTN_search.setOnClickListener {
            executeSearch()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            categoryManager.handleCategoryClick(category) //On category clicked
        }

        homePage_RV_categories.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = categoryAdapter
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun loadCategories() {
        categoriesList.value = categoryManager.getAllCategories()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun observeCategories() {
        categoriesList.observe(viewLifecycleOwner) { categoryList ->
            categoryAdapter.updateCategories(categoryList)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun executeSearch() {
        val searchQuery = homepage_ET_search.text.toString().trim()

        //If the query is empty
        if (searchQuery.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.enter_product_name), Toast.LENGTH_SHORT).show()
            return
        }

        //Check if there are products that match the search
        inventoryManager.searchProducts(requireContext(), searchQuery, null) { products ->
            if (products.isNotEmpty()) {
                //If products are found - pass the search term to the ProductsListFragment
                navigateToProductsListWithSearchQuery(searchQuery)
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // navigate to product list fragment after execute search query
    private fun navigateToProductsListWithSearchQuery(searchQuery: String) {
        val bundle = Bundle().apply {
            putString("SEARCH_QUERY", searchQuery)
            putBoolean("FROM_SEARCH", true)
        }

        (activity as? MainActivity)?.transactionToAnotherFragment(
            Constants.Fragment.PRODUCTSLIST, bundle)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Update the UI with user information
    private fun updateUIWithUserData(userData: UserHandlerManager.UserData) {
        homepage_TV_name.text = userData.firstName

        //Load profile picture
        if (!userData.profileImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(userData.profileImageUrl)
                .placeholder(R.drawable.profile_man)
                .error(R.drawable.profile_man)
                .into(homepage_IMG_profile)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    //Loading user details
    private fun loadUserProfile() {
        if (userHandlerManager.isUserLoggedIn()) {
            userHandlerManager.getCurrentUserData()?.let { userData ->
                updateUIWithUserData(userData)
            }
            userHandlerManager.loadUserProfile { result ->
                result.onSuccess { userData ->
                    activity?.runOnUiThread {
                        updateUIWithUserData(userData)
                    }
                }.onFailure { exception ->
                    activity?.runOnUiThread {
                        homepage_TV_name.text = getString(R.string.guest)
                        Log.e("HomePageFragment", "Error loading profile", exception)
                    }
                }
            }
        } else {
            homepage_TV_name.text = getString(R.string.guest)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////
}