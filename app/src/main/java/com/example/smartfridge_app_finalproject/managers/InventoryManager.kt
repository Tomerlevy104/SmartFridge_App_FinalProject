package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class InventoryManager: IInventoryManager {

    private val allProductsInInventory = ProductRepository()

    override fun getProductsByCategory(category: String): List<Product> {
        return when (category) {
            Constants.Category.FRUITS_AND_VEGETABLES -> allProductsInInventory.getFruitsAndVegetables()
            Constants.Category.DRINKS -> allProductsInInventory.getDrinks()
            Constants.Category.MEAT -> allProductsInInventory.getMeatAndFish()
            Constants.Category.ORGANICANDHEALTH -> allProductsInInventory.getOrganicAndHealth()
            Constants.Category.SNACKS -> allProductsInInventory.getSnacks()
            Constants.Category.BREADS -> allProductsInInventory.getBreads()
            Constants.Category.CLEANING -> allProductsInInventory.getCleaning()
            Constants.Category.COOKINGBAKING -> allProductsInInventory.getCookingAndBaking()
            Constants.Category.ANIMALS -> allProductsInInventory.getPetProducts()
            Constants.Category.DAIRY -> allProductsInInventory.getDairy()
            Constants.Category.BABYS -> allProductsInInventory.getBabyProducts()
            Constants.Category.FROZEN -> allProductsInInventory.getFrozen()
            else -> emptyList()
        }
    }

    override fun getAllProducts(): Flow<List<Product>> {
        return flowOf(allProductsInInventory.getInitialProducts())
    }

}