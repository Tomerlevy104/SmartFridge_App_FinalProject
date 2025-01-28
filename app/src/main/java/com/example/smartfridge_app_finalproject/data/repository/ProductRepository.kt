package com.example.smartfridge_app_finalproject.data.repository

import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.interfaces.IProductRepository
import com.example.smartfridge_app_finalproject.utilities.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ProductRepository : IProductRepository {
    //הפונקציה מחזירה רשימה של מוצרים. היא קוראת לכל הפונקציות שמחזירות מוצרים לפי הקטגוריה שלהן, וכל פונקציה כזאת מחזירה רשימה של מוצרים. הכוכבית פורס את כל המערכים לרשימה אחת גדולה
    override fun getInitialProducts(): List<Product> = listOf(
        *getFruitsAndVegetables().toTypedArray(),
        *getDrinks().toTypedArray(),
        *getMeatAndFish().toTypedArray(),
        *getOrganicAndHealth().toTypedArray(),
        *getSnacks().toTypedArray(),
        *getBreads().toTypedArray(),
        *getCleaning().toTypedArray(),
        *getCookingAndBaking().toTypedArray(),
        *getPetProducts().toTypedArray(),
        *getDairy().toTypedArray(),
        *getBabyProducts().toTypedArray(),
        *getFrozen().toTypedArray()
    )

    override fun getProductsByCategory(category: String): List<Product> {
        return when (category) {
            Constants.Category.FRUITS_AND_VEGETABLES -> getFruitsAndVegetables()
            Constants.Category.DRINKS -> getDrinks()
            Constants.Category.MEAT -> getMeatAndFish()
            Constants.Category.ORGANICANDHEALTH -> getOrganicAndHealth()
            Constants.Category.SNACKS -> getSnacks()
            Constants.Category.BREADS -> getBreads()
            Constants.Category.CLEANING -> getCleaning()
            Constants.Category.COOKINGBAKING -> getCookingAndBaking()
            Constants.Category.ANIMALS -> getPetProducts()
            Constants.Category.DAIRY -> getDairy()
            Constants.Category.BABYS -> getBabyProducts()
            Constants.Category.FROZEN -> getFrozen()
            else -> emptyList()
        }
    }

    private fun getDefaultExpiryDate(): String {
        val twoWeeksFromNow = LocalDate.now().plusWeeks(2)
        return twoWeeksFromNow.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }

    private fun getFruitsAndVegetables() = listOf(
        Product(
            barCode = "FV001",
            name = "תפוח עץ",
            category = Constants.Category.FRUITS_AND_VEGETABLES,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FV002",
            name = "בננה",
            category = Constants.Category.FRUITS_AND_VEGETABLES,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FV003",
            name = "עגבנייה",
            category = Constants.Category.FRUITS_AND_VEGETABLES,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FV004",
            name = "מלפפון",
            category = Constants.Category.FRUITS_AND_VEGETABLES,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FV005",
            name = "גזר",
            category = Constants.Category.FRUITS_AND_VEGETABLES,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getDrinks() = listOf(
        Product(
            barCode = "DR001",
            name = "מים מינרלים",
            category = Constants.Category.DRINKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "DR002",
            name = "קולה",
            category = Constants.Category.DRINKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "DR003",
            name = "מיץ תפוזים",
            category = Constants.Category.DRINKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "DR004",
            name = "ספרייט",
            category = Constants.Category.DRINKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getMeatAndFish() = listOf(
        Product(
            barCode = "MF001",
            name = "חזה עוף",
            category = Constants.Category.MEAT,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "MF002",
            name = "סלמון",
            category = Constants.Category.MEAT,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "MF003",
            name = "בשר טחון",
            category = Constants.Category.MEAT,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "MF004",
            name = "כרעי עוף",
            category = Constants.Category.MEAT,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getOrganicAndHealth() = listOf(
        Product(
            barCode = "OH001",
            name = "קינואה אורגנית",
            category = Constants.Category.ORGANICANDHEALTH,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "OH002",
            name = "טחינה אורגנית",
            category = Constants.Category.ORGANICANDHEALTH,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "OH003",
            name = "חלב שקדים",
            category = Constants.Category.ORGANICANDHEALTH,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getSnacks() = listOf(
        Product(
            barCode = "SN001",
            name = "במבה",
            category = Constants.Category.SNACKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "SN002",
            name = "ביסלי",
            category = Constants.Category.SNACKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "SN003",
            name = "צ׳יפס",
            category = Constants.Category.SNACKS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getBreads() = listOf(
        Product(
            barCode = "BR001",
            name = "לחם אחיד",
            category = Constants.Category.BREADS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "BR002",
            name = "פיתות",
            category = Constants.Category.BREADS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "BR003",
            name = "לחמניות",
            category = Constants.Category.BREADS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getCleaning() = listOf(
        Product(
            barCode = "CL001",
            name = "אקונומיקה",
            category = Constants.Category.CLEANING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "CL002",
            name = "סבון כלים",
            category = Constants.Category.CLEANING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "CL003",
            name = "נייר טואלט",
            category = Constants.Category.CLEANING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getCookingAndBaking() = listOf(
        Product(
            barCode = "CB001",
            name = "קמח",
            category = Constants.Category.COOKINGBAKING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "CB002",
            name = "סוכר",
            category = Constants.Category.COOKINGBAKING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "CB003",
            name = "שמן",
            category = Constants.Category.COOKINGBAKING,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getPetProducts() = listOf(
        Product(
            barCode = "PT001",
            name = "מזון לחתולים",
            category = Constants.Category.ANIMALS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "PT002",
            name = "מזון לכלבים",
            category = Constants.Category.ANIMALS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "PT003",
            name = "חול לחתולים",
            category = Constants.Category.ANIMALS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getDairy() = listOf(
        Product(
            barCode = "DA001",
            name = "חלב",
            category = Constants.Category.DAIRY,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "DA002",
            name = "גבינה צהובה",
            category = Constants.Category.DAIRY,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "DA003",
            name = "יוגורט",
            category = Constants.Category.DAIRY,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getBabyProducts() = listOf(
        Product(
            barCode = "BP001",
            name = "חיתולים",
            category = Constants.Category.BABYS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "BP002",
            name = "מטרנה",
            category = Constants.Category.BABYS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "BP003",
            name = "מגבונים",
            category = Constants.Category.BABYS,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )

    private fun getFrozen() = listOf(
        Product(
            barCode = "FR001",
            name = "פיצה קפואה",
            category = Constants.Category.FROZEN,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FR002",
            name = "ירקות קפואים",
            category = Constants.Category.FROZEN,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        ),
        Product(
            barCode = "FR003",
            name = "גלידה",
            category = Constants.Category.FROZEN,
            imageUrl = "",
            quantity = 1,
            expiryDate = getDefaultExpiryDate()
        )
    )
}