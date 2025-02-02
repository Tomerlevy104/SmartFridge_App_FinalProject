package com.example.smartfridge_app_finalproject.data.model

data class Product(val barCode: String,
                   val name: String,
                   val category: String,
                   val imageUrl: Int,
                   var quantity: Int,
                   val expiryDate: String)
