package com.example.smartfridge_app_finalproject.data.repository

import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Category
import com.example.smartfridge_app_finalproject.utilities.Constants

class CategoryRepository {

    fun getInitialCategories(): List<Category> = listOf(
        Category(
            id = "CAT_FRUITS_AND_VEGETABLES",
            name = Constants.Category.FRUITS_AND_VEGETABLES,
            categoryImage = R.drawable.category_fruits_veg
        ),
        Category(
            id = "CAT_DRINKS",
            name = Constants.Category.DRINKS,
            categoryImage = R.drawable.category__wine_drinks
        ),
        Category(
            id = "CAT_MEAT",
            name = Constants.Category.MEAT,
            categoryImage = R.drawable.category_meat
        ),
        Category(
            id = "CAT_ORGANICANDHEALTH",
            name = Constants.Category.ORGANICANDHEALTH,
            categoryImage = R.drawable.category_oregany
        ),
        Category(
            id = "CAT_SNACKS",
            name = Constants.Category.SNACKS,
            categoryImage = R.drawable.category_snacks
        ),
        Category(
            id = "CAT_BREADS",
            name = Constants.Category.BREADS,
            categoryImage = R.drawable.category_bread
        ),
        Category(
            id = "CAT_CLEANING",
            name = Constants.Category.CLEANING,
            categoryImage = R.drawable.category_clean
        ),
        Category(
            id = "CAT_COOKINGBAKING",
            name = Constants.Category.COOKINGBAKING,
            categoryImage = R.drawable.category_cooking_baking
        ),
        Category(
            id = "CAT_ANIMALS",
            name = Constants.Category.ANIMALS,
            categoryImage = R.drawable.category_animals
        ),
        Category(
            id = "CAT_DAIRY",
            name = Constants.Category.DAIRY,
            categoryImage = R.drawable.category_milk_eggs
        ),
        Category(
            id = "CAT_BABYS",
            name = Constants.Category.BABYS,
            categoryImage = R.drawable.category_babys
        ),
        Category(
            id = "CAT_FROZEN",
            name = Constants.Category.FROZEN,
            categoryImage = R.drawable.category_freez
        ),
        Category(
            id = "CAT_OTHER",
            name = Constants.Category.OTHER,
            categoryImage = R.drawable.category_others
        )
    )

    companion object {
        //Mapping between category code to category image
        val CATEGORY_IMAGE_MAP = mapOf(
            Constants.Category.FRUITS_AND_VEGETABLES to R.drawable.category_fruits_veg,
            Constants.Category.DRINKS to R.drawable.category__wine_drinks,
            Constants.Category.MEAT to R.drawable.category_meat,
            Constants.Category.ORGANICANDHEALTH to R.drawable.category_oregany,
            Constants.Category.SNACKS to R.drawable.category_snacks,
            Constants.Category.BREADS to R.drawable.category_bread,
            Constants.Category.CLEANING to R.drawable.category_clean,
            Constants.Category.COOKINGBAKING to R.drawable.category_cooking_baking,
            Constants.Category.ANIMALS to R.drawable.category_animals,
            Constants.Category.DAIRY to R.drawable.category_milk_eggs,
            Constants.Category.BABYS to R.drawable.category_babys,
            Constants.Category.FROZEN to R.drawable.category_freez,
            Constants.Category.OTHER to R.drawable.category_others
        )
    }
}