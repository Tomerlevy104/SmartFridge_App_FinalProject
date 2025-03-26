package com.example.smartfridge_app_finalproject.fragments

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.data.repository.CategoryRepository
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.example.smartfridge_app_finalproject.data.repository.ProductRepositoryService
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textview.MaterialTextView
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Fragment for manually adding products to the user's inventory.
 * This fragment provides a user interface for adding product details manually
 */
class AddProductManualFragment : Fragment() {

    private val TAG = "AddProductManualFragment"

    // UI components
    private lateinit var categoryDropdown: AutoCompleteTextView
    private lateinit var categoryLayout: TextInputLayout
    private lateinit var expiryDateEdit: TextInputEditText
    private lateinit var productNameEdit: TextInputEditText
    private lateinit var quantityEdit: TextInputEditText
    private lateinit var productBarCodeEdit: TextInputEditText
    private lateinit var productImageView: ShapeableImageView
    private lateinit var cameraButton: MaterialButton
    private lateinit var galleryButton: MaterialButton
    private lateinit var addButton: MaterialButton
    private lateinit var titleTextView: MaterialTextView

    // Managers
    private var inventoryManager = InventoryManager()
    private val validInputManager = ValidInputManager.getInstance()
    private val categoryRepository = CategoryRepository()
    private val productRepositoryService = ProductRepositoryService()

    // Date handling
    private val calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Image handling
    private var selectedImageUri: Uri? = null
    private var tempCameraUri: Uri? = null

    // User
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    // Repository related
    private var prefillFromRepository = false
    private var prefillScannedBarcode = ""
    private var prefillProductName = ""
    private var prefillCategory = ""
    private var prefillImageUrl = ""

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get product details for prefilling
        arguments?.let { args ->
            prefillScannedBarcode = args.getString("BARCODE", "")
            prefillProductName = args.getString("NAME", "")
            prefillCategory = args.getString("CATEGORY", "")
            prefillImageUrl = args.getString("IMAGE_URL", "")
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        initViews()
        setupCategoryDropdown()
        setupDatePicker()

        // Prefill fields from bundle data
        if (prefillScannedBarcode.isNotEmpty()) {
            productBarCodeEdit.setText(prefillScannedBarcode)
        }

        if (prefillProductName.isNotEmpty()) {
            productNameEdit.setText(prefillProductName)
        }

        if (prefillCategory.isNotEmpty()) {
            categoryDropdown.setText(prefillCategory)
        }

        // Set default quantity to 1
        if (quantityEdit.text.isNullOrEmpty()) {
            quantityEdit.setText("1")
        }

        // Load image if URL provided
        if (prefillImageUrl.isNotEmpty()) {
            try {
                selectedImageUri = Uri.parse(prefillImageUrl)
                Glide.with(requireContext())
                    .load(selectedImageUri)
                    .placeholder(R.drawable.category_no_picture)
                    .error(R.drawable.category_no_picture)
                    .into(productImageView)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading image from URL: ${e.message}")
            }
        }

        // Clear the expiry date field to force the user to select a date
        expiryDateEdit.text?.clear()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product_manual, container, false)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupViews(view: View) {
        categoryDropdown = view.findViewById(R.id.add_product_manual_ACTV_category)
        categoryLayout = view.findViewById(R.id.add_product_manual_TIL_category)
        productBarCodeEdit = view.findViewById(R.id.add_product_manual_EDT_barcode)
        expiryDateEdit = view.findViewById(R.id.add_product_manual_EDT_expiry)
        productNameEdit = view.findViewById(R.id.add_product_manual_EDT_name)
        quantityEdit = view.findViewById(R.id.add_product_manual_EDT_quantity)
        addButton = view.findViewById(R.id.add_product_manual_BTN_add)
        titleTextView = view.findViewById(R.id.add_product_manual_TV_title)

        // Product image components
        productImageView = view.findViewById(R.id.add_product_manual_IMG_product)
        cameraButton = view.findViewById(R.id.add_product_manual_BTN_camera)
        galleryButton = view.findViewById(R.id.add_product_manual_BTN_gallery)
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun initViews() {
        addButton.setOnClickListener {
            handleAddProduct()
        }

        productNameEdit.setOnClickListener { productNameEdit.error = null }
        quantityEdit.setOnClickListener { quantityEdit.error = null }
        categoryDropdown.setOnClickListener { categoryLayout.error = null }
        expiryDateEdit.setOnClickListener {
            expiryDateEdit.error = null
            showDatePicker()
        }

        // Initialize image selection buttons
        cameraButton.setOnClickListener {
            handleCameraRequest()
        }

        galleryButton.setOnClickListener {
            handleGalleryRequest()
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Permission and result launchers
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showToast(getString(R.string.camera_access_permission_is_required_to_select_a_product_image))
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showToast(getString(R.string.gallery_access_permission_is_required_to_select_a_product_image))
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempCameraUri?.let { uri ->
                selectedImageUri = uri
                loadImageIntoView(uri)
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            loadImageIntoView(it)
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun handleCameraRequest() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionExplanationDialog(PermissionType.CAMERA)
            }

            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun handleGalleryRequest() {
        val permission =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }

            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionExplanationDialog(PermissionType.GALLERY)
            }

            else -> {
                galleryPermissionLauncher.launch(permission)
            }
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun openCamera() {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "Product Image")
                put(MediaStore.Images.Media.DESCRIPTION, "Captured with Smart Fridge app")
            }

            tempCameraUri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            tempCameraUri?.let { uri ->
                cameraLauncher.launch(uri)
            } ?: run {
                showToast(getString(R.string.error_creating_temporary_camera_file))
            }
        } catch (e: Exception) {
            Log.e(TAG, getString(R.string.error_opening_the_camera) + " ${e.message}", e)
            showToast(getString(R.string.error_opening_the_camera))
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}", e)
            showToast(getString(R.string.error_opening_the_gallery))
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun loadImageIntoView(uri: Uri) {
        Glide.with(requireContext())
            .load(uri)
            .placeholder(R.drawable.category_no_picture)
            .error(R.drawable.category_no_picture)
            .into(productImageView)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showPermissionExplanationDialog(permissionType: PermissionType) {
        val message = when (permissionType) {
            PermissionType.CAMERA -> "אנחנו צריכים גישה למצלמה כדי לצלם תמונת מוצר"
            PermissionType.GALLERY -> "אנחנו צריכים גישה לגלריה כדי לבחור תמונת מוצר"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("נדרשת הרשאה")
            .setMessage(message)
            .setPositiveButton("אישור") { _, _ ->
                when (permissionType) {
                    PermissionType.CAMERA -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    PermissionType.GALLERY -> {
                        val permission =
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                        galleryPermissionLauncher.launch(permission)
                    }
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun validateFields(): Boolean {
        var isValid = true

        // Barcode
        val barcode = productBarCodeEdit.text.toString().trim()
        if (!validInputManager.isValidBarcode(barcode)) {
            productBarCodeEdit.error = getString(R.string.valid_input_product_barcode)
            isValid = false
        }

        // Product Name
        val productName = productNameEdit.text.toString().trim()
        if (!validInputManager.isValidProductName(productName)) {
            productNameEdit.error = getString(R.string.valid_input_product_name)
            isValid = false
        }

        // Category
        val category = categoryDropdown.text.toString()
        if (!validInputManager.isValidCategory(category)) {
            categoryLayout.error =
                getString(R.string.valid_input_please_select_a_category_from_the_list)
            isValid = false
        }

        // Quantity
        val quantity = quantityEdit.text.toString().toIntOrNull() ?: 0
        if (!validInputManager.isValidQuantity(quantity)) {
            quantityEdit.error = getString(R.string.valid_input_quantity_must_be_between_and)
            isValid = false
        }

        // Expiry Date
        val expiryDate = expiryDateEdit.text.toString()
        if (!validInputManager.isValidExpiryDate(expiryDate)) {
            expiryDateEdit.error =
                getString(R.string.valid_input_please_select_a_valid_expiration_date)
            isValid = false
        }

        return isValid
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun handleAddProduct() {
        if (!validateFields()) {
            return
        }
        val barCode = productBarCodeEdit.text.toString()
        val name = productNameEdit.text.toString()
        val category = categoryDropdown.text.toString()
        val quantity = quantityEdit.text.toString().toIntOrNull() ?: 0
        val expiryDate = expiryDateEdit.text.toString()
        val imageUri = selectedImageUri ?: Uri.EMPTY

        val product = Product(barCode = barCode, name = name, category = category, quantity = quantity,
            expiryDate = expiryDate, imageUrl = imageUri)

        // Add to inventory
        addProductToUserInventory(product)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun addProductToUserInventory(product: Product) {
        // Show loading state
        setLoadingState(true)

        if (userId == null) {
            showToast(getString(R.string.no_user_loged_in))
            setLoadingState(false)
            return
        }

        inventoryManager.handleAddProduct(product) { result ->
            requireActivity().runOnUiThread {
                result.onSuccess {
                    showToast(getString(R.string.added_successfully, product.name))
                    clearFields()

                    // Navigate to products list
                    (activity as? MainActivity)?.transactionToAnotherFragment(
                        Constants.Fragment.PRODUCTSLIST)
                }.onFailure { exception ->
                    showToast(exception.message ?: getString(R.string.product_not_added))
                }
                setLoadingState(false)
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setLoadingState(isLoading: Boolean) {
        addButton.isEnabled = !isLoading // If "isLoading" == true - you can't press the button
        addButton.text =
            if (isLoading) getString(R.string.adding) else getString(R.string.add_product)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun clearFields() {
        productBarCodeEdit.text?.clear()
        productNameEdit.text?.clear()
        categoryDropdown.text?.clear()
        quantityEdit.text?.clear()
        expiryDateEdit.text?.clear()
        selectedImageUri = null
        productImageView.setImageResource(R.drawable.category_no_picture)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupCategoryDropdown() {
        val categories = categoryRepository.getInitialCategories()
        val categoryNames = categories.map { it.name }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            categoryNames
        )
        categoryDropdown.setAdapter(adapter)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun setupDatePicker() {
        expiryDateEdit.setOnClickListener {
            expiryDateEdit.error = null
            showDatePicker()
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showDatePicker() {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                expiryDateEdit.error = null
                updateDateInView()
            },

            // The following three parameters determine the start date to be displayed in the dialog
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun updateDateInView() {
        expiryDateEdit.setText(dateFormatter.format(calendar.time))
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}