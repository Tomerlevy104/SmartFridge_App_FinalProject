package com.example.smartfridge_app_finalproject

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.smartfridge_app_finalproject.fragments.HomePageFragment
import com.example.smartfridge_app_finalproject.fragments.InventoryListFragment
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViews()
        initViews()
        //window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // אם אין פרגמנט פעיל, נציג את דף הפתיחה
        if (savedInstanceState == null) {
            transactionToAnotherFragment(Constants.Activities.STARTINGPAGE)
        }
    }

    private fun initViews() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> transactionToAnotherFragment(Constants.Activities.HOMEPAGE)
                R.id.nav_inventory -> transactionToAnotherFragment(Constants.Activities.INVENTORYLIST)
//                R.id.nav_add_product -> transactionToAnotherFragment(Constants.Activities.ADDPRODUCTSCANBARCODE)
//                R.id.nav_shopping_list -> transactionToAnotherFragment(Constants.Activities.CREATESHOPINGLIST)
//                R.id.nav_profile -> transactionToAnotherFragment(Constants.Activities.PROFILE)
            }
            true
        }
    }

    fun transactionToAnotherFragment(fragmentName: String, args: Bundle? = null) {
        val targetFragment = when (fragmentName) {
            Constants.Activities.HOMEPAGE -> {
                val fragment = HomePageFragment()
                args?.let { fragment.arguments = it }
                fragment
            }
            Constants.Activities.INVENTORYLIST -> {
                val fragment = InventoryListFragment()
                args?.let { fragment.arguments = it }
                fragment
            }
            Constants.Activities.ADDPRODUCTSCANBARCODE -> {
                // val fragment = AddProductScanBarCodeFragment()
                // args?.let { fragment.arguments = it }
                // fragment
                null
            }
            Constants.Activities.CREATESHOPINGLIST -> {
                // val fragment = CreateShoppingListFragment()
                // args?.let { fragment.arguments = it }
                // fragment
                null
            }
            Constants.Activities.PROFILE -> {
                // val fragment = ProfileFragment()
                // args?.let { fragment.arguments = it }
                // fragment
                null
            }
            else -> null
        }

        targetFragment?.let {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentContainer, it)
                .addToBackStack(null)  // מאפשר חזרה לפרגמנט הקודם
                .commit()
        }
    }

    private fun findViews() {
        bottomNavigation = findViewById(R.id.main_activity_bottomNavigation)
    }

    override fun onBackPressed() {
        // אם יש יותר מפרגמנט אחד במחסנית, נחזור לפרגמנט הקודם
        if (supportFragmentManager.backStackEntryCount > 1) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}