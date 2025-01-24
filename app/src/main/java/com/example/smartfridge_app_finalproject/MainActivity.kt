package com.example.smartfridge_app_finalproject

import android.content.Intent
import android.os.Bundle
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

    private fun transactionToAnotherFragment(fragmentName: String) {
        val targetFragment = when (fragmentName) {
            Constants.Activities.HOMEPAGE -> HomePageFragment()
            Constants.Activities.INVENTORYLIST-> InventoryListFragment()

//            Constants.Activities.ADDPRODUCTSCANBARCODE -> Intent(this, AddProductScanBarCodeFragment::class.java)
//            Constants.Activities.CREATESHOPINGLIST -> Intent(this, CreateShoppingListFragment::class.java)
//            Constants.Activities.PROFILE -> Intent(this, ProfileFragment::class.java)
//            Constants.Activities.LOGIN-> Intent(this,LoginActivity::class.java)
//            Constants.Activities.STARTINGPAGE-> Intent(this,StartingPageActivity::class.java)
//            Constants.Activities.ADDPRODUCTSCANBARCODE-> Intent(this,AddProductScanBarCodeActivity::class.java)
//            Constants.Activities.ADDPRODUCTMANUAL-> Intent(this,AddProductManualActivity::class.java)
//            Constants.Activities.CREATESHOPINGLIST-> Intent(this,CreateShoppingListActivity::class.java)
//            Constants.Activities.PROFILE-> Intent(this,ProfileActivity::class.java)
//            Constants.Activities.SUPERMARKETLOCATION-> Intent(this,SuperMarketLoactionActivity::class.java)
//            Constants.Activities.REGISTER-> Intent(this,RegisterActivity::class.java)
            else -> null
        }

        targetFragment?.let {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.contentContainer, it)
                .commit()
        }
    }











    private fun findViews() {
        bottomNavigation = findViewById(R.id.main_activity_bottomNavigation)
    }

}