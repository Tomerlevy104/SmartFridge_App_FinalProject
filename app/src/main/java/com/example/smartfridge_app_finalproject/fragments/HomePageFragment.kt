package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.CategoryAdapter
import com.example.smartfridge_app_finalproject.data.model.Category
import com.example.smartfridge_app_finalproject.managers.CategoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants

class HomePageFragment : Fragment() {

    private lateinit var categoryManager: CategoryManager
    private val categoriesList = MutableLiveData<List<Category>>()
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var homePage_RV_categories: RecyclerView
    private lateinit var homepage_BTN_show_all: AppCompatButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        categoryManager = CategoryManager(activity as MainActivity)
        findViews(view)
        setupClickListeners()
        setupRecyclerView()
        loadCategories()
        observeCategories()
    }

    private fun findViews(view: View) {
        homePage_RV_categories = view.findViewById(R.id.homePage_RV_categories)
        homepage_BTN_show_all = view.findViewById(R.id.homepage_BTN_show_all)
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

    private fun setupClickListeners() {
        homepage_BTN_show_all.setOnClickListener {
            //Show all button click
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
        }
    }
}
