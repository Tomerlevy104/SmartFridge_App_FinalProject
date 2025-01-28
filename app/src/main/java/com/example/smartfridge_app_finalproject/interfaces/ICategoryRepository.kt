package com.example.smartfridge_app_finalproject.interfaces

import com.example.smartfridge_app_finalproject.data.model.Category

interface ICategoryRepository {
    fun getAllCategories(): List<Category>
    fun getCategoryById(id: String): Category?
//    fun insertCategory(category: Category)
//    fun updateCategory(category: Category)
//    fun deleteCategory(id: String)
}