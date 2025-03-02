package com.example.smartfridge_app_finalproject.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.ProductsListAdapter
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.ProductRepository
import com.example.smartfridge_app_finalproject.interfaces.IInventoryManager
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class ProductsListFragment : Fragment() {
    private lateinit var productsListAdapter: ProductsListAdapter //Adapter
    private var LocalProductsListInInventory = mutableListOf<Product>() //Products List
    private val productRepository = ProductRepository.getInstance()

    private val inventoryManager: IInventoryManager = InventoryManager() //Inventory manager
    private lateinit var product_list_BTN_categories: AppCompatButton
    private lateinit var products_list_BTN_search: MaterialButton
    private lateinit var products_list_ET_search: AppCompatEditText
    private lateinit var products_list_TV_categoryName: MaterialTextView
    private lateinit var products_list_IMG_category: AppCompatImageView
    private lateinit var products_list_IMG_profile: ShapeableImageView
    private lateinit var products_list_TV_name: MaterialTextView
    private lateinit var products_list_IV_location: AppCompatImageButton
    private val userHandler = UserHandler.getInstance()
    private var selectedCategory: String? = null
    private var selectedCategoryImage: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        selectedCategory = arguments?.getString("SELECTED_CATEGORY")
        selectedCategoryImage = arguments?.getInt("SELECTED_CATEGORY_IMAGE")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_products_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        setupClickListeners()
        setupRecyclerView(view)
        loadProducts()
        loadUserProfile()
    }

    private fun findViews(view: View) {
        product_list_BTN_categories = view.findViewById(R.id.products_list_BTN_categories)
        products_list_TV_categoryName = view.findViewById(R.id.products_list_TV_categoryName)
        products_list_IMG_category = view.findViewById(R.id.products_list_IMG_category)
        products_list_BTN_search = view.findViewById(R.id.products_list_BTN_search)
        products_list_ET_search = view.findViewById(R.id.products_list_ET_search)
        products_list_IMG_profile = view.findViewById(R.id.products_list_IMG_profile)
        products_list_TV_name = view.findViewById(R.id.products_list_TV_name)
        products_list_IV_location = view.findViewById(R.id.products_list_IV_location)
    }

    private fun setupClickListeners() {
        //Categories button click
        product_list_BTN_categories.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.HOMEPAGE)
        }

        products_list_BTN_search.setOnClickListener {
            executeSearch()
        }

        products_list_IV_location.setOnClickListener {
            (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.SUPERMARKET)
        }
    }

    private fun loadProducts() {
        LocalProductsListInInventory.clear()  //Clear the existing local list
        updateCategoryUI()  //Update UI with selected category

        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(),
                getString(R.string.no_user_loged_in), Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedCategory != null) {
            loadProductsByCategory(currentUser.uid, selectedCategory!!)
        } else {
            loadAllProducts(currentUser.uid)
        }
    }

    private fun updateCategoryUI() {
        //Setting the title of the selected category
        products_list_TV_categoryName.text = selectedCategory

        //Setting the category image
        if (selectedCategoryImage != null) {
            products_list_IMG_category.visibility = View.VISIBLE
            products_list_IMG_category.setImageResource(selectedCategoryImage!!)
        } else {
            products_list_IMG_category.visibility = View.GONE
        }
    }

    //Load all products in inventory
    private fun loadAllProducts(userId: String) { //צריך להעביר אותה לרפוזיטורי של המאגר??????????
        val productsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("products")

        productsRef.get()
            .addOnSuccessListener { documents ->
                processProductDocuments(documents)
            }
            .addOnFailureListener { exception ->
                handleProductLoadError(exception)
            }
    }


    //Load all products according to category name
    private fun loadProductsByCategory(userId: String, category: String) {
         inventoryManager.getProductsByCategory(userId,category)
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////
//    //Load all products according to category name
//    private fun loadProductsByCategory(userId: String, category: String) { //צריך להעביר אותה לרפוזיטורי של המאגר??????????
//        val productsRef = FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(userId)
//            .collection("products")
//            .whereEqualTo("category", category)
//
//        productsRef.get()
//            .addOnSuccessListener { documents ->
//                processProductDocuments(documents)
//            }
//            .addOnFailureListener { exception ->
//                handleProductLoadError(exception)
//            }
//    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    //Searching product according exact name
//    private fun searchProductByName(productName: String, onComplete: (Product?) -> Unit) {
//        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
//            Toast.makeText(requireContext(), "משתמש לא מחובר", Toast.LENGTH_SHORT).show()
//            onComplete(null)
//            return
//        }
//
//        val productsRef = FirebaseFirestore.getInstance()
//            .collection("users")
//            .document(currentUser.uid)
//            .collection("products")
//            .whereEqualTo("name", productName)
//
//        productsRef.get()
//            .addOnSuccessListener { documents ->
//                if (documents.isEmpty) {
//                    onComplete(null)
//                } else {
//                    try {
//                        val document = documents.documents[0]
//                        val product = Product(
//                            barCode = document.getString("barCode") ?: "",
//                            name = document.getString("name") ?: "",
//                            category = document.getString("category") ?: "",
//                            imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) } ?: Uri.EMPTY,
//                            quantity = document.getLong("quantity")?.toInt() ?: 0,
//                            expiryDate = document.getString("expiryDate") ?: ""
//                        )
//                        onComplete(product)
//                    } catch (e: Exception) {
//                        Log.e("ProductsList", "Error converting document to Product", e)
//                        onComplete(null)
//                    }
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("ProductsList", "Error searching product by name", exception)
//                onComplete(null)
//            }
//    }


    /////////////////////////////////////////////////////////////////////////////////////////////////////

    //Search for products containing the search term
    private fun searchProductsContaining(searchString: String, onComplete: (List<Product>) -> Unit) { //צריך להעביר אותה לרפוזיטורי של המאגר??????????
        val currentUser = FirebaseAuth.getInstance().currentUser ?: run {
            Toast.makeText(requireContext(), "משתמש לא מחובר", Toast.LENGTH_SHORT).show()
            onComplete(emptyList())
            return
        }

        val lowerCaseSearchString = searchString.lowercase()

        //Product from fire store
        val productsRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(currentUser.uid)
            .collection("products")

        //If a specific category is selected, we will filter only the products from that category
        val productsList = if (selectedCategory != null) {
            productsRef.whereEqualTo("category", selectedCategory)
        } else {
            productsRef
        }

        productsList.get()
            .addOnSuccessListener { documentsFromProductList ->
                val matchingProducts = mutableListOf<Product>()

                for (document in documentsFromProductList) {
                    try {
                        val productName = document.getString("name") ?: continue

                        //Checks if the product name contains the search string
                        if (productName.lowercase().contains(lowerCaseSearchString)) {
                            val product = Product(
                                barCode = document.getString("barCode") ?: continue,
                                name = productName,
                                category = document.getString("category") ?: continue,
                                imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) } ?: Uri.EMPTY,
                                quantity = document.getLong("quantity")?.toInt() ?: continue,
                                expiryDate = document.getString("expiryDate") ?: continue
                            )
                            matchingProducts.add(product)
                        }
                    } catch (e: Exception) {
                        Log.e("ProductsList", "Error converting document to Product", e)
                        continue
                    }
                }

                onComplete(matchingProducts)
            }
            .addOnFailureListener { exception ->
                Log.e("ProductsList", "Error searching products", exception)
                onComplete(emptyList())
            }
    }

    /**
     * מעבד את התוצאות שהתקבלו מהשאילתא ומעדכן את הרשימה
     */
    private fun processProductDocuments(documents: QuerySnapshot) {
        LocalProductsListInInventory.clear()
        for (document in documents) {
            try {
                val product = Product(
                    barCode = document.getString("barCode") ?: continue,
                    name = document.getString("name") ?: continue,
                    category = document.getString("category") ?: continue,
                    imageUrl = document.getString("imageUrl")?.let { Uri.parse(it) } ?: Uri.EMPTY,
                    quantity = document.getLong("quantity")?.toInt() ?: continue,
                    expiryDate = document.getString("expiryDate") ?: continue
                )
                LocalProductsListInInventory.add(product)
            } catch (e: Exception) {
                Log.e("ProductsList", "Error converting document to Product", e)
                continue
            }
        }
        productsListAdapter.notifyDataSetChanged()
    }

    /**
     * מטפל בשגיאות בטעינת מוצרים
     */
    private fun handleProductLoadError(exception: Exception) {
        Log.e("ProductsList", "Error getting products", exception)
        Toast.makeText(requireContext(), "שגיאה בטעינת המוצרים", Toast.LENGTH_SHORT).show()
    }

    private fun loadUserProfile() {
        if (userHandler.isUserLoggedIn()) {
            //First, we check if there is data in local memory
            userHandler.getCurrentUserData()?.let { userData ->
                updateUIWithUserData(userData)
            }

            // נטען/נרענן את הנתונים מ-Firestore
            userHandler.loadUserProfile { result ->
                result.onSuccess { userData ->
                    activity?.runOnUiThread {
                        updateUIWithUserData(userData)
                    }
                }.onFailure { exception ->
                    activity?.runOnUiThread {
                        products_list_TV_name.text = "אורח"
                        Log.e("ProductsListFragment", "Error loading profile", exception)
                    }
                }
            }
        } else {
            products_list_TV_name.text = "אורח"
        }
    }

    private fun updateUIWithUserData(userData: UserHandler.UserData) {
        products_list_TV_name.text = userData.firstName

        // טעינת תמונת הפרופיל
        if (!userData.profileImageUrl.isNullOrEmpty()) {
            Glide.with(requireContext())
                .load(userData.profileImageUrl)
                .placeholder(R.drawable.profile_man)
                .error(R.drawable.profile_man)
                .into(products_list_IMG_profile)
        }
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<RecyclerView>(R.id.products_list_RV_products)
        productsListAdapter = ProductsListAdapter(
            products = LocalProductsListInInventory,
            onQuantityChanged = { product, newQuantity ->
                product.quantity = newQuantity
                productsListAdapter.notifyDataSetChanged()
            },
            onRemoveClicked = { product ->
                LocalProductsListInInventory.remove(product)
                productsListAdapter.notifyDataSetChanged()
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = productsListAdapter
    }



    /**
     * מבצע חיפוש מוצרים לפי מחרוזת החיפוש
     */
    private fun executeSearch() {
        val searchQuery = products_list_ET_search.text.toString().trim()

        if (searchQuery.isNotEmpty()) {
            searchProductsContaining(searchQuery) { products ->
                if (products.isNotEmpty()) {
                    // נמצאו מוצרים - מציג אותם
                    LocalProductsListInInventory.clear()
                    LocalProductsListInInventory.addAll(products)
                    productsListAdapter.notifyDataSetChanged()
                } else {
                    // לא נמצאו מוצרים
                    Toast.makeText(requireContext(), "לא נמצאו מוצרים מתאימים", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            // אם שדה החיפוש ריק, מציג את כל המוצרים או את המוצרים מהקטגוריה הנוכחית
            loadProducts()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // רענון נתונים בכל חזרה למסך
    }
}