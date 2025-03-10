package com.example.smartfridge_app_finalproject.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.smartfridge_app_finalproject.MainActivity
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.data.model.Product
import com.example.smartfridge_app_finalproject.managers.InventoryManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.data.repository.ProductRepositoryService
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textview.MaterialTextView
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import androidx.camera.view.PreviewView
import android.net.Uri
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddProductBarCodeFragment : Fragment() {

    // Tag for logging
    private val TAG = "AddProductBarCodeFragment"

    // Camera and barcode scanning components
    private lateinit var cameraExecutor: ExecutorService  // Executor for camera processing tasks
    private lateinit var barcodeScanner: BarcodeScanner   // ML Kit barcode scanner

    // UI components
    private lateinit var barCode_PV_preview: PreviewView   // Camera preview
    private lateinit var barCode_TV_result: MaterialTextView  // Text view to show barcode result
    private lateinit var barCode_BTN_add: MaterialButton  // Button to add product

    // Managers and services
    private val userHandler = UserHandler.getInstance()  // Handles user authentication
    private val inventoryManager = InventoryManager()    // Manages product inventory
    private val productRepositoryService = ProductRepositoryService()  // Service to get product info

    // State variables
    private var lastScannedBarcode: String? = null  // Last detected barcode value
    private var isScanningPaused = false  // Flag to pause scanning after detection


    /**
     * Permission launcher for camera
     */
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // If permission granted, start the camera
            startCamera()
        } else {
            // If permission denied, show message and exit
            Toast.makeText(requireContext(),
                getString(R.string.camera_permission_is_required_to_scan_a_barcode), Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_product_bar_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Initialize UI components and setup functionality
        findViews(view)
        setupScanner()
        checkCameraPermission()
        setupListeners()
    }

    private fun findViews(view: View) {
        barCode_PV_preview = view.findViewById(R.id.barCode_PV_preview)
        barCode_TV_result = view.findViewById(R.id.barCode_TV_result)
        barCode_BTN_add = view.findViewById(R.id.barCode_BTN_add)
    }

    /**
     * Setup the barcode scanner with appropriate options
     */
    private fun setupScanner() {
        // Configure scanner options to detect common barcode formats
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,  // European Article Number
                Barcode.FORMAT_EAN_8,   // European Article Number
                Barcode.FORMAT_UPC_A,   // Universal Product Code
                Barcode.FORMAT_UPC_E,   // Universal Product Code
                Barcode.FORMAT_CODE_128 // High-density linear barcode (used in logistics)
            )
            .build()

        // Create barcode scanner with specified options
        barcodeScanner = BarcodeScanning.getClient(options)

        // Create executor for camera operations on a background thread
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    /**
     * Check camera permission
     */
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted
            startCamera()
        } else {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    /**
     * Setup button click listeners
     */
    private fun setupListeners() {
        barCode_BTN_add.setOnClickListener {
            lastScannedBarcode?.let { barcode ->
                // If a barcode has been scanned, show dialog to add product
                showProductDialog(barcode)
            } ?: run {
                // If no barcode has been scanned yet
                Toast.makeText(requireContext(),
                    getString(R.string.please_scan_barcode_first), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Initialize and start the camera for barcode scanning
     */
    private fun startCamera() {
        // Get instance of camera provider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Get the camera provider when it's ready
            val cameraProvider = cameraProviderFuture.get()

            // Set up camera preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(barCode_PV_preview.surfaceProvider)

            // Set up image analyzer for barcode scanning
            // STRATEGY_KEEP_ONLY_LATEST means discard older frames if processing is slow
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Set the analyzer which will process each frame
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            // Use back camera by default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any current use cases before binding new ones
                cameraProvider.unbindAll()

                // Bind the camera preview and image analysis to the lifecycle
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,  // Tie to fragment's lifecycle
                    cameraSelector,      // Select back camera
                    preview,             // Show camera preview on screen
                    imageAnalysis        // Process frames for barcode scanning
                )
            } catch (e: Exception) {
                // Handle camera binding errors
                Log.e(TAG, "Camera binding failed: ${e.message}", e)
                Toast.makeText(requireContext(),
                    getString(R.string.do_not_turn_on_the_camera), Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))  // Run on main thread
    }


    /**
     * Process each camera frame to detect barcodes
     * This function is the core of the barcode scanning mechanism in the app.
     */
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // If scanning is paused, close the image and return
        if (isScanningPaused) {
            imageProxy.close()
            return
        }

        // Get the image from the proxy
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            // Convert camera image to ML Kit's InputImage format
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees  // Keep proper orientation
            )

            // Process the image with the barcode scanner
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // If any barcodes were detected
                    if (barcodes.isNotEmpty()) {
                        // Get the first barcode detected (usually only one in frame)
                        val barcode = barcodes[0]
                        barcode.rawValue?.let { barcodeValue ->
                            // Pause scanning to avoid multiple detections
                            isScanningPaused = true

                            // Update UI on the main thread
                            requireActivity().runOnUiThread {
                                barCode_TV_result.text = barcodeValue
                                lastScannedBarcode = barcodeValue
                            }

                            // Resume scanning after a short delay
                            barCode_PV_preview.postDelayed({
                                isScanningPaused = false
                            }, 2000)  // 2 second delay
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle barcode scanning errors
                    Log.e(TAG, getString(R.string.barcode_scanning_failed, e.message), e)
                }
                .addOnCompleteListener {
                    // Always close the imageProxy after processing
                    imageProxy.close()
                }
        } else {
            // If no image available, just close the proxy
            imageProxy.close()
        }
    }

    /**
     * Show dialog to handle adding the product associated with the scanned barcode
     */
    private fun showProductDialog(barcode: String) {
        // First check if product exists in the database or product repository
        checkIfProductExists(barcode)
    }

    /**
     * Check if a product with the scanned barcode exists in the inventory
     * or in the product repository
     */
    private fun checkIfProductExists(barcode: String) {
        val currentUser = userHandler.getCurrentFirebaseUser()
        if (currentUser == null) {
            Toast.makeText(requireContext(), getString(R.string.no_user_loged_in), Toast.LENGTH_SHORT).show()
            return
        }

        // First check if the product exists in user's inventory
        try {
            inventoryManager.loadAllProducts { products ->
                // Find any product with matching barcode
                val existingProduct = products.firstOrNull { it.barCode == barcode }

                // Now check in the products repository if not found in user's inventory
                if (existingProduct != null) {
                    // Product exists in user's inventory - show dialog to increase quantity
                    activity?.runOnUiThread {
                        showProductExistsDialog(existingProduct)
                    }
                } else {
                    // Product not in inventory, check repository
                    checkProductInRepository(barcode)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking inventory: ${e.message}", e)
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_checking_inventory, e.message),
                    Toast.LENGTH_SHORT
                ).show()
                isScanningPaused = false
            }
        }
    }


    /**
     * Check if the product exists in the product repository (Fire store)
     */
    private fun checkProductInRepository(barcode: String) {
        try {
            // Use the ProductRepositoryService to check if product exists
            productRepositoryService.getProductByBarcode(barcode) { result ->
                activity?.runOnUiThread {
                    if (result != null) {
                        // Product found in repository - show dialog to add it to inventory
                        showProductFromRepositoryDialog(result, barcode)
                    } else {
                        // Product not found anywhere - show dialog to add a new product
                        showProductNotFoundDialog(barcode)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any exceptions during the repository check
            Log.e(TAG, getString(R.string.error_checking_product_repository, e.message), e)
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_checking_product_database, e.message),
                    Toast.LENGTH_SHORT
                ).show()
                // Show the regular not-found dialog as fallback
                showProductNotFoundDialog(barcode)
            }
        }
    }

    /**
     * Show dialog for adding a product that exists in the repository but not in user's inventory
     */
    private fun showProductFromRepositoryDialog(repositoryProduct: Map<String, Any>, barcode: String) {
        val productName = repositoryProduct["name"] as? String ?: getString(R.string.unknown_product)
        val category = repositoryProduct["category"] as? String ?: Constants.Category.OTHER
        val imageUrl = repositoryProduct["imageUrl"] as? String

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.product_is_in_repository))
            .setMessage(
                getString(
                    R.string.founded_the_product_X_do_you_want_to_add_it_to_your_inventory,
                    productName
                ))
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                // Add product to user's inventory from repository data
                addProductFromRepository(barcode, productName, category, imageUrl)
            }
            .setNegativeButton(getString(R.string.edit_details)) { _, _ ->
                // Open manual add screen with prefilled details
                val bundle = Bundle().apply {
                    putString("BARCODE", barcode)
                    putString("NAME", productName)
                    putString("CATEGORY", category)
                    imageUrl?.let { putString("IMAGE_URL", it) }
                }
                // Going to "Add manual fragment"
                (activity as? MainActivity)?.transactionToAnotherFragment(
                    Constants.Fragment.ADDPRODUCTMANUAL,
                    bundle
                )
            }
            .setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    /**
     * Add a product to user's inventory using data from the repository
     */
    private fun addProductFromRepository(
        barcode: String,
        name: String,
        category: String,
        imageUrl: String?
    ) {
        // Create product object with data from repository
        val product = Product(
            barCode = barcode,
            name = name,
            category = category,
            imageUrl = imageUrl?.let { Uri.parse(it) } ?: Uri.EMPTY,
            quantity = 1,
            expiryDate = LocalDate.now().plusMonths(3).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        )

        // Add product to database
        addProductToInventory(product)
    }

    /**
     * Show dialog for a product that already exists in user's inventory
     */
    private fun showProductExistsDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.product_is_in_inventory))
            .setMessage(
                getString(
                    R.string.the_product_is_already_in_your_inventory_do_you_want_to_increase_the_quantity,
                    product.name
                ))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Increase product quantity by 1
                updateProductQuantity(product)
            }
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    /**
     * Update the quantity of an existing product
     */
    private fun updateProductQuantity(product: Product) {
        try {
            // Update product quantity in database
            inventoryManager.updateProductQuantity(product, product.quantity + 1) { result ->
                result.onSuccess {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(),
                            getString(R.string.product_quantity_updated_successfully), Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
                    }
                }.onFailure { exception ->
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_updating_product_quantity, exception.message),
                            Toast.LENGTH_SHORT
                        ).show()
                        isScanningPaused = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, getString(R.string.error_updating_product_quantity), e)
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_updating_product_quantity),
                    Toast.LENGTH_SHORT
                ).show()
                isScanningPaused = false
            }
        }
    }

    /**
     * Show dialog for a product that doesn't exist anywhere
     */
    private fun showProductNotFoundDialog(barcode: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.new_product))
            .setMessage(
                getString(
                    R.string.barcode_founded_X_do_you_want_to_add_a_new_product_to_inventory,
                    barcode
                ))
            .setPositiveButton(getString(R.string.add_manually)) { _, _ ->
                // Open manual add screen with scanned barcode
                val bundle = Bundle().apply {
                    putString("BARCODE", barcode)
                }
                (activity as? MainActivity)?.transactionToAnotherFragment(
                    Constants.Fragment.ADDPRODUCTMANUAL,
                    bundle
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    /**
     * Add a product to the user's inventory
     */
    private fun addProductToInventory(product: Product) {
        try {
            inventoryManager.addProduct(product) { result ->
                result.onSuccess {
                    activity?.runOnUiThread {
                        Toast.makeText(requireContext(),
                            getString(R.string.product_added_successfully), Toast.LENGTH_SHORT).show()
                        (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
                    }
                }.onFailure { exception ->
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_adding_product, exception.message),
                            Toast.LENGTH_SHORT
                        ).show()
                        isScanningPaused = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, getString(R.string.error_adding_product), e)
            activity?.runOnUiThread {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_adding_product),
                    Toast.LENGTH_SHORT
                ).show()
                isScanningPaused = false
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Shut down the executor when fragment is destroyed
        cameraExecutor.shutdown()
    }
}