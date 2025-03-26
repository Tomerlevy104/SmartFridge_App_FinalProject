package com.example.smartfridge_app_finalproject.interfaces

import com.example.smartfridge_app_finalproject.data.model.Category

interface ICategoryManager {

    /**
     * Retrieves all available product categories
     * @return A list of all Category objects in the system
     */
    fun getAllCategories(): List<Category>
}