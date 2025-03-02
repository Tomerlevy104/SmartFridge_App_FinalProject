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
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AddProductBarCodeFragment : Fragment() {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var barcodeScanner: BarcodeScanner
    private lateinit var previewView: PreviewView
    private lateinit var tvResult: MaterialTextView
    private lateinit var btnAdd: MaterialButton
    private var lastScannedBarcode: String? = null
    private var isScanningPaused = false
    private val inventoryManager = InventoryManager() // הסרת getInstance()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(requireContext(), "הרשאת מצלמה נדרשת לסריקת ברקוד", Toast.LENGTH_LONG).show()
            requireActivity().onBackPressed()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_product_bar_code, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // אתחול רכיבי ממשק
        previewView = view.findViewById(R.id.barcode_preview_view)
        tvResult = view.findViewById(R.id.barcode_TV_result)
        btnAdd = view.findViewById(R.id.barcode_BTN_add)

        // הגדרת אפשרויות הסורק
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A,
                Barcode.FORMAT_UPC_E,
                Barcode.FORMAT_CODE_128
            )
            .build()

        // יצירת סורק ברקודים
        barcodeScanner = BarcodeScanning.getClient(options)

        // יצירת Executor לפעולות המצלמה
        cameraExecutor = Executors.newSingleThreadExecutor()

        // בדיקת הרשאות מצלמה
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        // הגדרת לחצן הוספת מוצר
        btnAdd.setOnClickListener {
            lastScannedBarcode?.let { barcode ->
                showProductDialog(barcode)
            } ?: run {
                Toast.makeText(requireContext(), "נא לסרוק ברקוד תחילה", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // הגדרת Preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            // מנתח תמונה לסריקת ברקודים
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                processImageProxy(imageProxy)
            }

            // בחירת מצלמה אחורית כברירת מחדל
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // התחל מחדש את הסורק
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e(TAG, "כשל בהפעלת המצלמה: ${e.message}", e)
                Toast.makeText(requireContext(), "לא ניתן להפעיל את המצלמה", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun processImageProxy(imageProxy: ImageProxy) {
        if (isScanningPaused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees
            )

            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes[0]
                        barcode.rawValue?.let { barcodeValue ->
                            isScanningPaused = true
                            requireActivity().runOnUiThread {
                                tvResult.text = barcodeValue
                                lastScannedBarcode = barcodeValue

                                // אופציונלי: רטט או צליל כדי לציין סריקה מוצלחת
                            }

                            // חזור לסריקה אחרי 2 שניות
                            previewView.postDelayed({
                                isScanningPaused = false
                            }, 2000)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "כשל בסריקת הברקוד: ${e.message}", e)
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }

    private fun showProductDialog(barcode: String) {
        // ב-API שלך, תבדוק אם המוצר קיים בדרך אחרת
        checkIfProductExists(barcode)
    }

    private fun checkIfProductExists(barcode: String) {
        // כאן נשתמש בפונקציה שבדיקה אם המוצר קיים במסד הנתונים
        // במקום להשתמש ב-getProductByBarcode שלא קיים ב-API שלך

        // סימולציה - בדיקת המוצרים הקיימים בזיכרון
        val existingProduct = inventoryManager.findProductsByBarcode(barcode).firstOrNull()

        if (existingProduct != null) {
            // המוצר קיים - הצג דיאלוג האם להגדיל כמות
            showProductExistsDialog(existingProduct)
        } else {
            // המוצר לא קיים - הצג דיאלוג הוספת מוצר חדש
            showProductNotFoundDialog(barcode)
        }
    }

    private fun showProductExistsDialog(product: Product) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("מוצר קיים במלאי")
            .setMessage("המוצר ${product.name} כבר קיים במלאי שלך. האם ברצונך להגדיל את הכמות?")
            .setPositiveButton("כן") { _, _ ->
                // הגדלת כמות המוצר ב-1
                updateProductQuantity(product)
            }
            .setNegativeButton("לא") { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    private fun updateProductQuantity(product: Product) {
        // עדכון כמות המוצר במסד הנתונים
        val updatedProduct = product.copy(quantity = product.quantity + 1)

        inventoryManager.addProduct(updatedProduct) { result ->
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "כמות המוצר עודכנה בהצלחה", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
            } else {
                Toast.makeText(requireContext(), "שגיאה בעדכון כמות המוצר: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showProductNotFoundDialog(barcode: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("מוצר חדש")
            .setMessage("נמצא ברקוד: $barcode\nהאם ברצונך להוסיף מוצר חדש למלאי?")
            .setPositiveButton("הוסף ידנית") { _, _ ->
                // פתח את המסך להוספה ידנית עם הברקוד מוזן מראש
                val bundle = Bundle().apply {
                    putString("BARCODE", barcode)
                }
                (activity as? MainActivity)?.transactionToAnotherFragment(
                    Constants.Fragment.ADDPRODUCTMANUAL,
                    bundle
                )
            }
            .setNeutralButton("הוסף אוטומטית") { _, _ ->
                // הוספה אוטומטית של מוצר עם פרטים בסיסיים
                addDefaultProduct(barcode)
            }
            .setNegativeButton("בטל") { dialog, _ ->
                dialog.dismiss()
                isScanningPaused = false
            }
            .show()
    }

    private fun addDefaultProduct(barcode: String) {
        // יצירת מוצר ברירת מחדל עם הברקוד שנסרק
        val defaultProduct = Product(
            barCode = barcode,
            name = "מוצר $barcode",
            category = Constants.Category.OTHER,
            imageUrl = Uri.EMPTY,
            quantity = 1,
            expiryDate = LocalDate.now().plusWeeks(2).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        )

        // הוספת המוצר למאגר
        inventoryManager.addProduct(defaultProduct) { result ->
            if (result.isSuccess) {
                Toast.makeText(requireContext(), "המוצר נוסף בהצלחה", Toast.LENGTH_SHORT).show()
                (activity as? MainActivity)?.transactionToAnotherFragment(Constants.Fragment.PRODUCTSLIST)
            } else {
                Toast.makeText(requireContext(), "שגיאה בהוספת המוצר: ${result.exceptionOrNull()?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // פונקציית עזר לחיפוש מוצרים לפי ברקוד
    private fun InventoryManager.findProductsByBarcode(barcode: String): List<Product> {
        return this.findProductsByName(barcode) // שימוש לצורך דוגמה בלבד, רצוי להוסיף פונקציה ייעודית
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "BarcodeScannerFragment"
    }
}