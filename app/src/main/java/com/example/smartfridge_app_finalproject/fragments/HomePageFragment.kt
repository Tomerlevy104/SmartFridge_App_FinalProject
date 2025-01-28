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
import com.example.smartfridge_app_finalproject.data.repository.CategoryRepository
import com.example.smartfridge_app_finalproject.interfaces.ICategoryRepository
import com.example.smartfridge_app_finalproject.utilities.Constants

class HomePageFragment : Fragment() {
    private lateinit var homePage_RV_categories: RecyclerView
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var homepage_BTN_show_all: AppCompatButton
    private lateinit var categoryRepository: ICategoryRepository
    private val categories = MutableLiveData<List<Category>>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeRepository()
        findViews(view)
        setupRecyclerView()
        loadCategories()
        observeCategories()
        setupClickListeners()
    }

    private fun initializeRepository() {
        categoryRepository = CategoryRepository()
    }

    private fun findViews(view: View) {
        homePage_RV_categories = view.findViewById(R.id.homePage_RV_categories)
        homepage_BTN_show_all = view.findViewById(R.id.homepage_BTN_show_all)
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            handleCategoryClick(category)
        }

        homePage_RV_categories.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = categoryAdapter
        }
    }

    private fun loadCategories() {
        //Using the repository to get categories
        categories.value = categoryRepository.getAllCategories()
    }

    private fun observeCategories() {
        categories.observe(viewLifecycleOwner) { categoryList ->
            categoryAdapter.updateCategories(categoryList)
        }
    }

    private fun setupClickListeners() {
        homepage_BTN_show_all.setOnClickListener {
            // כשלוחצים על "הצג הכל" אין צורך להעביר Bundle
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Activities.INVENTORYLIST)
        }
    }

    private fun handleCategoryClick(category: Category) {
        val bundle = Bundle().apply {
            putString("SELECTED_CATEGORY", category.name)
        }

        (activity as? MainActivity)?.transactionToAnotherFragment(
            Constants.Activities.INVENTORYLIST,
            bundle
        )
    }
}
