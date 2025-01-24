package com.example.smartfridge_app_finalproject.utilities

data class Product(val barCode: String,
                   val name: String,
                   val category: String,
                   val imageUrl: String,
                   var quantity: Int,
                   val expiryDate: String)
