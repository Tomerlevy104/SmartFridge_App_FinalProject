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
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Fragment for scanning product barcodes with the device camera
 */
class AddProductBarCodeFragment : Fragment() {

    private val TAG = "AddProductBarCodeFragment"

    // Camera and barcode scanning components
    private lateinit var cameraExecutor: ExecutorService  // Executor for camera processing tasks
    private lateinit var barcodeScanner: BarcodeScanner   // Barcode scanner - allows to recognize and process barcodes in real time

    // UI components
    private lateinit var barCode_PV_preview: PreviewView   // Camera preview
    private lateinit var barCode_TV_result: MaterialTextView  // Text view to show barcode result
    private lateinit var barCode_BTN_add: MaterialButton  // Button to add product

    // Managers and services
    private val inventoryManager = InventoryManager()    // Manages product inventory
    private val productRepositoryService =
        ProductRepositoryService()  // Service to get product info

    // State variables
    private var lastScannedBarcode: String? = null  // Last detected barcode value
    private var isScanningPaused = false  // Flag to pause scanning after detection

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Permission launcher for camera
    // Handles the result of the permission request after the user has already responded to it (approved or denied)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // If permission granted, start the camera
                startCamera()
            } else {
                // If permission denied, show message and exit
                Toast.makeText(
                    requireContext(),
                    getString(R.string.camera_permission_is_required_to_scan_a_barcode),
                    Toast.LENGTH_LONG
                ).show()
                // Returns back to the page we came from if there is no camera permission
                requireActivity().onBackPressedDispatcher.onBackPressed()
                //requireActivity().onBackPressed()
            }
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_product_bar_code, container, false)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Initialize UI components and setup functionality
        findViews(view)
        setupScanner()
        checkCameraPermission() // First, checking camera permission
        setupListeners()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    private fun findViews(view: View) {
        barCode_PV_preview = view.findViewById(R.id.barCode_PV_preview)
        barCode_TV_result = view.findViewById(R.id.barCode_TV_result)
        barCode_BTN_add = view.findViewById(R.id.barCode_BTN_add)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Setup the barcode scanner with appropriate options
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Check camera permission
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission already granted
            startCamera()
        } else {
            // Request permission
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Setup button click listeners
    private fun setupListeners() {
        barCode_BTN_add.setOnClickListener {
            lastScannedBarcode?.let { barcode ->
                // Adding process start
                showProductDialog(barcode) // If a barcode has been scanned, show dialog to add product
            } ?: run {
                // If no barcode has been scanned yet
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_scan_barcode_first), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    //Initialize and start the camera for barcode scanning
    private fun startCamera() {
        // Get instance of camera provider
        // Requests access to the main interface for controlling cameras on the device
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            // Get the camera provider when it's ready
            val cameraProvider = cameraProviderFuture.get()

            // Set up camera preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(barCode_PV_preview.surfaceProvider)

            // Real-time image analysis from the camera
            // STRATEGY_KEEP_ONLY_LATEST means discard older frames if processing is slow
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            // Defines what will happen to each image that comes from the camera
            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                // `ImageProxy` is an object that contains the image taken from the camera and other information related to it
                processImageProxy(imageProxy)
            }

            // Use back camera by default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind any current use cases before binding new ones
                cameraProvider.unbindAll()

                // Releases camera-related resources that were in use
                // Prepares the system to accept new camera settings
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
                Toast.makeText(
                    requireContext(),
                    getString(R.string.do_not_turn_on_the_camera), Toast.LENGTH_SHORT
                ).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))  // Run on main thread
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Process each camera frame to detect barcodes
    // This function is the core of the barcode scanning mechanism in the app
    @OptIn(ExperimentalGetImage::class)
    private fun processImageProxy(imageProxy: ImageProxy) {
        // If scanning is paused, close the image and return
        if (isScanningPaused) {
            imageProxy.close()
            return
        }

        // Get the image from the proxyImage
        val mediaImage = imageProxy.image // Access to the image data
        if (mediaImage != null) {
            // Convert camera image to barcode scanner InputImage format
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )  // Keep proper orientation

            // Process the image with the barcode scanner
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    // If any barcodes were detected
                    if (barcodes.isNotEmpty()) {
                        // Get the first barcode detected (usually only one in frame)
                        val barcode =
                            barcodes[0] // Refers to the first barcode identified in the list
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

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Displays appropriate dialog depending on barcode scanning results
    private fun showProductDialog(barcode: String) {
        // Check if the product is in the user's inventory
        inventoryManager.productIsExistInUserInventory(barcode) { existingProduct ->
            activity?.runOnUiThread {
                if (existingProduct != null) {
                    // The product is in user inventory - displays a quantity update dialog
                    showProductExistsInUserInventoryDialog(existingProduct)
                } else {
                    // Check if the product exists in Repository Of Products Collection
                    inventoryManager.productIsExistInRepositoryOfProductCollection(barcode) { existsInRepository ->
                        if (existsInRepository) {
                            // The product exists in the Repository Of Products Collection - get its details
                            try {
                                productRepositoryService.getProductByBarcode(barcode) { theProduct ->
                                    if (theProduct != null) {
                                        showProductFromRepositoryDialog(theProduct)
                                    } else {
                                        // The product exists in the database but we were unable to got it
                                        showProductNotFoundDialog(barcode)
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(
                                    TAG,
                                    "Error retrieving product from repository: ${e.message}",
                                    e
                                )
                                showProductNotFoundDialog(barcode)
                            }
                        } else {
                            // The product does not exist anywhere
                            showProductNotFoundDialog(barcode)
                        }
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Show dialog for a product that already exists in user's inventory
    private fun showProductExistsInUserInventoryDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.product_is_in_inventory))
            .setMessage(
                getString(
                    R.string.the_product_is_already_in_your_inventory_do_you_want_to_increase_the_quantity,
                    product.name
                )
            )
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                //Update the quantity of an existing product
                try {
                    inventoryManager.updateProductQuantity(
                        product.barCode,
                        product.quantity + 1
                    ) { result ->
                        activity?.runOnUiThread {
                            result.onSuccess {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.product_quantity_updated_successfully),
                                    Toast.LENGTH_SHORT
                                ).show()

                                (activity as? MainActivity)?.transactionToAnotherFragment(
                                    Constants.Fragment.PRODUCTSLIST
                                )
                            }.onFailure { exception ->
                                Toast.makeText(
                                    requireContext(),
                                    getString(
                                        R.string.error_updating_product_quantity,
                                        exception.message
                                    ),
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
            .setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////
    // Show dialog for adding a product that exists in the repository but not in user's inventory
    private fun showProductFromRepositoryDialog(repositoryProduct: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.product_is_in_repository))
            .setMessage(
                getString(
                    R.string.founded_the_product_X_do_you_want_to_add_it_to_your_inventory,
                    repositoryProduct.name
                )
            )
            // Add button on dialog
            .setPositiveButton(getString(R.string.add)) { _, _ ->
                // Add product to user's inventory
                inventoryManager.handleAddProduct(repositoryProduct) { result ->
                    activity?.runOnUiThread {
                        result.onSuccess {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.added_successfully, repositoryProduct.name),
                                Toast.LENGTH_SHORT
                            ).show()

                            // Navigate to products list
                            (activity as? MainActivity)?.transactionToAnotherFragment(
                                Constants.Fragment.PRODUCTSLIST
                            )

                        }.onFailure { exception ->
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.error_adding_product, exception.message),
                                Toast.LENGTH_SHORT
                            ).show()
                            isScanningPaused = false
                        }
                    }
                }
            }
            // Cancel button on dialog
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            // Edit details button on dialog
            .setNeutralButton(getString(R.string.edit_details)) { _, _ ->
                // Open manual add screen with prefilled details
                val bundle = Bundle().apply {
                    putString("BARCODE", repositoryProduct.barCode)
                    putString("NAME", repositoryProduct.name)
                    putString("CATEGORY", repositoryProduct.category)
                    repositoryProduct.imageUrl.toString().let {
                        if (it != "null" && it.isNotEmpty()) {
                            putString("IMAGE_URL", it)
                        }
                    }
                }
                // Going to "Add manual fragment"
                (activity as? MainActivity)?.transactionToAnotherFragment(
                    Constants.Fragment.ADDPRODUCTMANUAL,
                    bundle
                )
            }
            .show()
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Show dialog for a product that doesn't exist anywhere
    private fun showProductNotFoundDialog(barcode: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.new_product))
            .setMessage(
                getString(
                    R.string.barcode_founded_X_do_you_want_to_add_a_new_product_to_inventory,
                    barcode
                )
            )
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // On destroy
    override fun onDestroy() {
        super.onDestroy()
        // Shut down the executor when fragment is destroyed
        cameraExecutor.shutdown()
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////
}