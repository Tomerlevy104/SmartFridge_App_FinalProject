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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import android.util.Log

class HomePageFragment : Fragment() {

    private lateinit var categoryManager: CategoryManager
    private var inventoryManager: InventoryManager = InventoryManager() //Inventory manager
    private val categoriesList = MutableLiveData<List<Category>>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var homePage_RV_categories: RecyclerView
    private lateinit var homepage_BTN_show_all: AppCompatButton
    private lateinit var homepage_ET_search: AppCompatEditText
    private lateinit var homepage_BTN_search: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var homepage_IMG_profile: ShapeableImageView
    private lateinit var homepage_TV_name: MaterialTextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        categoryManager = CategoryManager(activity as MainActivity)
        inventoryManager = InventoryManager()
        findViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadCategories()
        observeCategories()
        loadUserProfile()
    }

    private fun findViews(view: View) {
        homePage_RV_categories = view.findViewById(R.id.homePage_RV_categories)
        homepage_BTN_show_all = view.findViewById(R.id.homepage_BTN_show_all)
        homepage_ET_search = view.findViewById(R.id.products_list_ET_search)
        homepage_BTN_search = view.findViewById(R.id.homepage_BTN_search)
        // Add profile views
        homepage_IMG_profile = view.findViewById(R.id.homepage_IMG_profile)
        homepage_TV_name = view.findViewById(R.id.homepage_TV_name)
    }

    private fun setupClickListeners() {
        homepage_BTN_show_all.setOnClickListener {
            //Show all button click
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
        }
        homepage_BTN_search.setOnClickListener {
            executeSearch()
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            categoryManager.handleCategoryClick(category) //Click on category
        }

        homePage_RV_categories.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        categoriesList.value = categoryManager.getAllCategories()
    }

    private fun observeCategories() {
        categoriesList.observe(viewLifecycleOwner) { categoryList ->
            categoryAdapter.updateCategories(categoryList)
        }
    }

    private fun executeSearch() {
        val searchQuery = homepage_ET_search.text.toString().trim()
        inventoryManager.searchProduct(requireContext(), searchQuery)
    }

//    //Function to load user profile
//    private fun loadUserProfile() {
//        val currentUser = auth.currentUser
//        currentUser?.let { user ->
//            // נשנה לגישה ל-Firestore
//            firestore.collection("users").document(user.uid)
//                .get()
//                .addOnSuccessListener { document ->
//                    if (document != null && document.exists()) {
//                        // נשלוף את הנתונים מה-document
//                        val firstName = document.getString("firstName")
//                        val profileImageUrl = document.getString("profileImageUrl")
//
//                        // נעדכן את שם המשתמש
//                        homepage_TV_name.text = firstName ?: user.displayName ?: "אורח"
//
//                        // נטען את תמונת הפרופיל
//                        if (!profileImageUrl.isNullOrEmpty()) {
//                            Glide.with(requireContext())
//                                .load(profileImageUrl)
//                                .placeholder(R.drawable.profile_man)
//                                .error(R.drawable.profile_man)
//                                .into(homepage_IMG_profile)
//                        }
//                    } else {
//                        Log.d("HomePageFragment", "No such document")
//                        homepage_TV_name.text = user.displayName ?: "אורח"
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Log.e("HomePageFragment", "Error loading user profile", e)
//                    homepage_TV_name.text = user.displayName ?: "אורח"
//                }
//        }
//    }
}