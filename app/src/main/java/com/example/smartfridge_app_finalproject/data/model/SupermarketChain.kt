package com.example.smartfridge_app_finalproject.data.model

data class SupermarketChain(
    val id: String,
    val name: String
)

object SupermarketChains {
    val chains = listOf(
        SupermarketChain("1", "שופרסל"),
        SupermarketChain("2", "רמי לוי"),
        SupermarketChain("3", "יינות ביתן"),
        SupermarketChain("4", "ויקטורי"),
        SupermarketChain("5", "מגה"),
        SupermarketChain("6", "יוחננוף"),
        SupermarketChain("7", "אושר עד"),
        SupermarketChain("8", "חצי חינם"),
        SupermarketChain("9", "מחסני השוק"),
        SupermarketChain("10", "סופר יודה"),
    )
}