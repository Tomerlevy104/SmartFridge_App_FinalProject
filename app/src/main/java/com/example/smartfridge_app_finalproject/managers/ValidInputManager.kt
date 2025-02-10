package com.example.smartfridge_app_finalproject.managers

import com.example.smartfridge_app_finalproject.utilities.Constants
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ValidInputManager private constructor() {

    companion object {
        @Volatile
        private var instance: ValidInputManager? = null

        fun getInstance(): ValidInputManager =
            instance ?: synchronized(this) {
                instance ?: ValidInputManager().also { instance = it }
            }
    }

    //First name validation checker
    fun isValidFirstName(firstName: String): Boolean {
        if (firstName.length < 2) return false
        return firstName.all { it.isLetter() }
    }

    //Last name validation checker
    fun isValidLastName(lastName: String): Boolean {
        if (lastName.length < 2) return false
        return lastName.all { it.isLetter() }
    }

    //Barcode Validation Checker
    fun isValidBarcode(barcode: String): Boolean {
        //Check if barcode is empty or not numeric
        if (barcode.isBlank() || !barcode.all { it.isDigit() }) return false

        //Check if barcode starts with 2025
        if (!barcode.startsWith("2025")) return false

        //Additional check for reasonable barcode length (usually between 8-13 digits)
        if (barcode.length < 8 || barcode.length > 13) return false

        return true
    }

    //Password Validation Checker
    fun isValidPassword(password: String): Boolean {
        if (password.length < Constants.ValidInput.MIN_PASSWORD_LENGTH) return false

        //Check that there is at least one letter and one number
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }

        return hasLetter && hasDigit
    }

    //Product name checker
    fun isValidProductName(productName: String): Boolean {
        //Check that the name is not empty and does not exceed the maximum length
        if (productName.isBlank() || productName.length > Constants.ValidInput.MAX_PRODUCT_NAME_LENGTH) return false

        //Check that the name does not contain only numbers
        if (productName.all { it.isDigit() }) return false

        //Checking that there are no special characters (except spaces and hyphens)
        val validCharPattern = Regex("^[\\u0590-\\u05FFa-zA-Z0-9\\s-]+$")
        return productName.matches(validCharPattern)
    }

    //Checks the product quantity
    fun isValidQuantity(quantity: Int): Boolean {
        return quantity in 1..(Constants.ValidInput.MAX_QUANTITY)
    }

    //Email Validation Checker
    fun isValidEmail(email: String): Boolean {
        val emailPattern = Regex("[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return email.matches(emailPattern)
    }

    //Username Validation Checker
    fun isValidUsername(username: String): Boolean {
        if (username.length < 3 || username.length > 20) return false

        //Allows Hebrew, English letters, numbers and hyphens
        val validCharPattern = Regex("^[\\u0590-\\u05FFa-zA-Z0-9-]+$")
        return username.matches(validCharPattern)
    }

    //Checks the validity of an Israeli phone number
    fun isValidIsraeliPhone(phone: String): Boolean {
        //Cleans up spaces and hyphens
        val cleanPhone = phone.replace(Regex("[\\s-]"), "")

        //The first digit must be 0, followed by a number that is either 2,3,4,5 or 8 and then 8 digits
        return cleanPhone.matches(Regex("^(0[2-57])[0-9]{8}$"))
    }

    //Category Validator
    fun isValidCategory(category: String): Boolean {
        val validCategories = listOf(
            Constants.Category.FRUITS_AND_VEGETABLES,
            Constants.Category.DRINKS,
            Constants.Category.MEAT,
            Constants.Category.ORGANICANDHEALTH,
            Constants.Category.SNACKS,
            Constants.Category.BREADS,
            Constants.Category.CLEANING,
            Constants.Category.COOKINGBAKING,
            Constants.Category.ANIMALS,
            Constants.Category.DAIRY,
            Constants.Category.BABYS,
            Constants.Category.FROZEN,
            Constants.Category.OTHER
        )
        return category.isNotBlank() && validCategories.contains(category)
    }

    //Expiration date validation checker
    fun isValidExpiryDate(expiryDate: String?): Boolean {
        return !expiryDate.isNullOrBlank()
    }
}