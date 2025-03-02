package com.example.smartfridge_app_finalproject.utilities

class Constants {
    object Activities {
        const val STARTINGPAGE = "STARTINGPAGE"
        const val REGISTER = "REGISTER"
        const val LOGIN = "LOGIN"
    }

    object Fragment {
        const val HOMEPAGE = "HOMEPAGE"
        const val PRODUCTSLIST = "PRODUCTSLIST"
        const val ADDPRODUCTSCANBARCODE = "ADDPRODUCTSCANBARCODE"
        const val ADDPRODUCTMANUAL = "ADDPRODUCTMANUAL"
        const val CREATESHOPINGLIST = "CREATESHOPINGLIST"
        const val SHOPINGLIST = "SHOPINGLIST"
        const val PROFILE = "PROFILE"
        const val SUPERMARKET = "SUPERMARKET"    }

    object Category {
        const val FRUITS_AND_VEGETABLES = "פירות וירקות"
        const val DRINKS = "משקאות"
        const val MEAT = "בשר ודגים"
        const val ORGANICANDHEALTH = "אורגני ובריאות"
        const val SNACKS = "חטיפים, מתוקים ודגנים"
        const val BREADS = "לחמים ומוצרי מאפה"
        const val CLEANING = "ניקיון וחד פעמי"
        const val COOKINGBAKING = "בישול, אפיה ושימורים"
        const val ANIMALS = "בעלי חיים"
        const val DAIRY = "מוצרי חלב וביצים"
        const val BABYS = "תינוקות"
        const val FROZEN = "קפואים"
        const val OTHER = "אחר"
    }

    object ValidInput {
        const val MIN_PASSWORD_LENGTH = 6
        const val MAX_PRODUCT_NAME_LENGTH = 50
        const val MAX_QUANTITY = 999
    }

    object ImageUploadRequest {
        const val CAMERA_PERMISSION_REQUEST = 100
        const val GALLERY_PERMISSION_REQUEST = 101
        const val CAMERA_REQUEST_CODE = 102
        const val GALLERY_REQUEST_CODE = 103
    }

    object PermissionRequest {
        const val LOCATION_PERMISSION_REQUEST = 1
    }

}