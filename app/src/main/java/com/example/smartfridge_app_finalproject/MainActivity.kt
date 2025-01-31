package com.example.smartfridge_app_finalproject

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.core.view.forEach
import com.example.smartfridge_app_finalproject.fragments.HomePageFragment
import com.example.smartfridge_app_finalproject.fragments.ProductsListFragment
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

        //If no active fragment, move to "StartingPage"
        if (savedInstanceState == null) {
            transactionToAnotherFragment(Constants.Activities.STARTINGPAGE)
        }
    }

    private fun initViews() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> transactionToAnotherFragment(Constants.Fragment.HOMEPAGE)
                R.id.nav_inventory -> transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
                R.id.nav_add_product -> {
                    showAddProductPopup(bottomNavigation.findViewById(R.id.nav_add_product))
                    false // חשוב! מונע את בחירת הכפתור
                }
                R.id.nav_shopping_list -> transactionToAnotherFragment(Constants.Fragment.CREATESHOPINGLIST)
                R.id.nav_profile -> transactionToAnotherFragment(Constants.Fragment.PROFILE)
                else -> false
            }
            true
        }
    }

    fun transactionToAnotherFragment(fragmentName: String, args: Bundle? = null) {
        val targetFragment = when (fragmentName) {
            Constants.Fragment.HOMEPAGE -> {
                val fragment = HomePageFragment()
                args?.let { fragment.arguments = it }
                fragment
            }

            Constants.Fragment.PRODUCTSLIST -> {
                val fragment = ProductsListFragment()
                args?.let { fragment.arguments = it }
                fragment
            }

            Constants.Fragment.ADDPRODUCTSCANBARCODE -> {
                // val fragment = AddProductScanBarCodeFragment()
                // args?.let { fragment.arguments = it }
                // fragment
                null
            }

            Constants.Fragment.CREATESHOPINGLIST -> {
                // val fragment = CreateShoppingListFragment()
                // args?.let { fragment.arguments = it }
                // fragment
                null
            }

            Constants.Fragment.PROFILE -> {
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

    private fun showAddProductPopup(view: View) {
        try {
            val inflater = LayoutInflater.from(this)
            val popupView = inflater.inflate(R.layout.popup_add_product, null)

            // חישוב מימדים לפני יצירת PopupWindow
            popupView.measure(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val popupWindow = PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            ).apply {
                elevation = 10f
                isOutsideTouchable = true
                isFocusable = true

                // הוספת אנימציה קלה
                animationStyle = android.R.style.Animation_Dialog
            }

            // מאזיני לחיצה
            popupView.findViewById<LinearLayout>(R.id.manual_add)?.setOnClickListener {
                transactionToAnotherFragment(Constants.Fragment.ADDPRODUCTMANUAL)
                popupWindow.dismiss()
            }

            popupView.findViewById<LinearLayout>(R.id.barcode_add)?.setOnClickListener {
                transactionToAnotherFragment(Constants.Fragment.ADDPRODUCTSCANBARCODE)
                popupWindow.dismiss()
            }

            // מציאת המיקום של כפתור הוספת מוצר
            val addButtonLocation = IntArray(2)
            view.getLocationOnScreen(addButtonLocation)

            // הצגת הפופאפ במיקום מעל הכפתור
            val navigationBarHeight = resources.getDimensionPixelSize(
                resources.getIdentifier("navigation_bar_height", "dimen", "android")
            )

            popupWindow.showAtLocation(
                view,
                Gravity.NO_GRAVITY,
                addButtonLocation[0] - (popupView.measuredWidth / 20) + (view.width / 20), // ממורכז אופקית מעל הכפתור
                addButtonLocation[1] - popupView.measuredHeight - 10 // מעט מרווח מהכפתור
            )

        } catch (e: Exception) {
            Log.e("PopupError", "Error showing popup: ${e.message}", e)
            Toast.makeText(this, "אירעה שגיאה בפתיחת התפריט", Toast.LENGTH_SHORT).show()
        }
    }














    //////////////////////////////////////////////////////////////////////////////////////////////////
//    private fun showAddProductPopup(view: View) {
//        val popup = PopupMenu(this, view)
//        popup.menuInflater.inflate(R.menu.add_new_product_menu, popup.menu)
//
//        // מציג אייקונים גם בגרסאות ישנות של אנדרואיד
//        popup.menu.forEach { menuItem ->
//            menuItem.setShowAsAction(1) // SHOW_AS_ACTION_ALWAYS
//        }
//
//        popup.setOnMenuItemClickListener { item ->
//            when (item.itemId) {
//                R.id.manual_add -> {
//                    transactionToAnotherFragment(Constants.Fragment.ADDPRODUCTMANUAL)
//                    true
//                }
//                R.id.barcode_add -> {
//                    transactionToAnotherFragment(Constants.Fragment.ADDPRODUCTSCANBARCODE)
//                    true
//                }
//                else -> false
//            }
//        }
//
//        popup.show()
//    }



}