package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.InventoryAdapter
import com.example.smartfridge_app_finalproject.utilities.Product

class InventoryListFragment : Fragment() {
    private lateinit var adapter: InventoryAdapter
    private var products = mutableListOf<Product>()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_inventory_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        // להוסיף קריאה לפונקציה שמביאה את המוצרים מהמסד נתונים


        //manual adding product
        products.add(Product(
            barCode = "1",
            name = "בננה",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "2",
            name = "תפוז",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "3",
            name = "תפוח",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "4",
            name = "אבוקדו",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "5",
            name = "רימון",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "6",
            name = "נענע",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        products.add(Product(
            barCode = "7",
            name = "בצל",
            category = "Fruits",
            imageUrl = "",
            quantity = 2,
            expiryDate = "10/04/2025"))

        adapter.notifyDataSetChanged()

    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.inventory_list_RV_products)
        adapter = InventoryAdapter(
            products = products,
            onQuantityChanged = { product, newQuantity ->
                // עדכון הכמות במסד הנתונים
                product.quantity = newQuantity
                adapter.notifyDataSetChanged()
            },
            onRemoveClicked = { product ->
                // מחיקת המוצר מהמסד נתונים
                products.remove(product)
                adapter.notifyDataSetChanged()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

    }


}




















////////////////////////////////////////////////////////////////////////////////////////////////////////
//class InventoryListFragment : Fragment() {
//
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        arguments?.let {
//
//        }
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_inventory_list, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//
//    }
//
//}