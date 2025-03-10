package com.example.smartfridge_app_finalproject.managers

import android.os.Bundle
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.data.model.Category
import com.example.smartfridge_app_finalproject.data.repository.CategoryRepository
import com.example.smartfridge_app_finalproject.interfaces.ICategoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants

class CategoryManager(private val activity: MainActivity) : ICategoryManager {
    private val categoryRepository = CategoryRepository()

    override fun getAllCategories(): List<Category> {
        return categoryRepository.getInitialCategories()
    }

    override fun getCategoryById(id: String): Category? {
        return categoryRepository.getInitialCategories().find { it.id == id }
    }

    fun handleCategoryClick(category: Category) {
        val bundle = Bundle().apply {
            putString("SELECTED_CATEGORY", category.name)
            putInt("SELECTED_CATEGORY_IMAGE", category.categoryImage)
        }
        (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST, bundle)
    }
}