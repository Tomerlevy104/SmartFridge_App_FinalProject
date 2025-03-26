package com.example.smartfridge_app_finalproject

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.FileProvider
import com.example.smartfridge_app_finalproject.managers.UploadImageManager
import com.example.smartfridge_app_finalproject.managers.UsersManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.google.android.material.button.MaterialButton
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var register_TIL_firstname: TextInputLayout
    private lateinit var register_TIL_lastname: TextInputLayout
    private lateinit var register_TIL_email: TextInputLayout
    private lateinit var register_TIL_password: TextInputLayout
    private lateinit var register_TIL_confirmpassword: TextInputLayout

    private lateinit var register_ET_firstname: AppCompatEditText
    private lateinit var register_ET_lastname: AppCompatEditText
    private lateinit var register_ET_email: AppCompatEditText
    private lateinit var register_ET_password: AppCompatEditText
    private lateinit var register_ET_confirmpassword: AppCompatEditText

    private lateinit var register_IMG_profile: ShapeableImageView
    private lateinit var register_BTN_camera: MaterialButton
    private lateinit var register_BTN_gallery: MaterialButton
    private lateinit var register_BTN_submit: MaterialButton
    private lateinit var register_BTN_back: MaterialButton


    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private var uploadImageManager = UploadImageManager.getInstance()
    private var validInputManager = ValidInputManager.getInstance()
    private lateinit var usersManager: UsersManager

    private val TAG = "RegisterActivity"

    // ActivityResultLaunchers for permissions and image selection
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "הרשאת מצלמה נדרשת לצילום תמונת פרופיל", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchGallery()
        } else {
            Toast.makeText(this, "הרשאת גלריה נדרשת לבחירת תמונת פרופיל", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleCameraResult(result.data)
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleGalleryResult(result.data)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        usersManager = UsersManager(this)
        uploadImageManager = UploadImageManager.getInstance()
        validInputManager = ValidInputManager.getInstance()

        findViews()
        initViews()
    }

    private fun findViews() {
        // TextInputLayouts
        register_TIL_firstname = findViewById(R.id.register_TIL_firstname)
        register_TIL_lastname = findViewById(R.id.register_TIL_lastname)
        register_TIL_email = findViewById(R.id.register_TIL_email)
        register_TIL_password = findViewById(R.id.register_TIL_password)
        register_TIL_confirmpassword = findViewById(R.id.register_TIL_confirmpassword)

        // EditTexts
        register_ET_firstname = findViewById(R.id.register_ET_firstname)
        register_ET_lastname = findViewById(R.id.register_ET_lastname)
        register_ET_email = findViewById(R.id.register_ET_email)
        register_ET_password = findViewById(R.id.register_ET_password)
        register_ET_confirmpassword = findViewById(R.id.register_ET_confirmpassword)

        // Buttons and ImageView
        register_IMG_profile = findViewById(R.id.register_IMG_profile)
        register_BTN_camera = findViewById(R.id.register_BTN_camera)
        register_BTN_gallery = findViewById(R.id.register_BTN_gallery)
        register_BTN_submit = findViewById(R.id.register_BTN_submit)
        register_BTN_back = findViewById(R.id.register_BTN_back)

    }

    private fun initViews() {
        setupTextChangeListeners()

        register_BTN_submit.setOnClickListener {
            if (validateForm()) {
                handleRegistration()
            }
        }

        register_BTN_camera.setOnClickListener {
            handleCameraButtonClick()
        }

        register_BTN_gallery.setOnClickListener {
            handleGalleryButtonClick()
        }

        register_BTN_back.setOnClickListener {
            navigateToStartingPage()
        }
    }

    // Navigate back to StartingPageActivity
    private fun navigateToStartingPage() {
        val intent = Intent(this, StartingPageActivity::class.java)
        startActivity(intent)
        finish() // Close current activity
    }

    private fun setupTextChangeListeners() {
        val fieldPairs = mapOf(
            register_ET_firstname to register_TIL_firstname,
            register_ET_lastname to register_TIL_lastname,
            register_ET_email to register_TIL_email,
            register_ET_password to register_TIL_password,
            register_ET_confirmpassword to register_TIL_confirmpassword
        )

        for ((editText, inputLayout) in fieldPairs) {
            editText.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) inputLayout.error = null
            }
        }
    }

    private fun validateForm(): Boolean {
        var isValid = true
        val firstName = register_ET_firstname.text.toString().trim()
        val lastName = register_ET_lastname.text.toString().trim()
        val email = register_ET_email.text.toString().trim()
        val password = register_ET_password.text.toString()
        val confirmPassword = register_ET_confirmpassword.text.toString()

        // First Name validation
        if (!validInputManager.isValidFirstName(firstName)) {
            register_TIL_firstname.error = getString(R.string.invalid_first_name)
            isValid = false
        }

        // Last Name validation
        if (!validInputManager.isValidLastName(lastName)) {
            register_TIL_lastname.error = getString(R.string.invalid_last_name)
            isValid = false
        }

        // Email validation
        if (!validInputManager.isValidEmail(email)) {
            register_TIL_email.error = getString(R.string.invalid_email)
            isValid = false
        }

        // Password validation
        if (!validInputManager.isValidPassword(password)) {
            register_TIL_password.error = getString(R.string.password_must_contain_at_least)
            isValid = false
        }

        // Confirm password validation
        if (password != confirmPassword) {
            register_TIL_confirmpassword.error = getString(R.string.mismatched_passwords)
            isValid = false
        }

        return isValid
    }

    // Handle camera button click
    private fun handleCameraButtonClick() {
        if (uploadImageManager.checkCameraPermission(this)) {
            launchCamera()
        } else {
            if (uploadImageManager.shouldShowRequestPermissionRationale(this, PermissionType.CAMERA)) {
                showPermissionRationale(PermissionType.CAMERA)
            } else {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    // Handle gallery button click
    private fun handleGalleryButtonClick() {
        if (uploadImageManager.checkGalleryPermission(this)) {
            launchGallery()
        } else {
            if (uploadImageManager.shouldShowRequestPermissionRationale(this, PermissionType.GALLERY)) {
                showPermissionRationale(PermissionType.GALLERY)
            } else {
                val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    android.Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                }
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    // Launch camera using a direct ContentResolver approach
    private fun launchCamera() {
        try {
            // Create content values for the temporary image
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "Profile Image")
                put(MediaStore.Images.Media.DESCRIPTION, "Temporary profile image for SmartFridge app")
            }

            // Get URI from content resolver
            cameraImageUri = contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            )

            // Check if URI was created successfully
            if (cameraImageUri == null) {
                Log.e(TAG, "Failed to create camera image URI")
                Toast.makeText(this, "לא ניתן ליצור URI לתמונת מצלמה", Toast.LENGTH_SHORT).show()
                return
            }

            // Create camera intent
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }

            // Check if there's an app to handle this intent
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                cameraLauncher.launch(takePictureIntent)
            } else {
                Toast.makeText(this, "אין אפליקציית מצלמה זמינה במכשיר", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}", e)
            Toast.makeText(this, "שגיאה בפתיחת המצלמה: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Launch gallery using the new approach
    private fun launchGallery() {
        try {
            val intent = uploadImageManager.createGalleryIntent()
            galleryLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}", e)
            Toast.makeText(this, "שגיאה בפתיחת הגלריה", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle camera result
    private fun handleCameraResult(data: Intent?) {
        try {
            // If we used EXTRA_OUTPUT, the image is stored in cameraImageUri
            if (cameraImageUri != null) {
                selectedImageUri = cameraImageUri
                register_IMG_profile.setImageURI(cameraImageUri)
                Log.d(TAG, "Camera image URI: $cameraImageUri")
            }
            // Fallback to the thumbnail if for some reason cameraImageUri is null
            else if (data?.extras?.get("data") != null) {
                val bitmap = data.extras?.get("data") as Bitmap
                val uri = getImageUriFromBitmap(bitmap)
                selectedImageUri = uri
                register_IMG_profile.setImageBitmap(bitmap)
                Log.d(TAG, "Camera thumbnail URI: $uri")
            } else {
                Log.e(TAG, "No camera data received")
                Toast.makeText(this, "לא התקבלה תמונה מהמצלמה", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing camera image: ${e.message}", e)
            Toast.makeText(this, "שגיאה בעיבוד תמונת המצלמה", Toast.LENGTH_SHORT).show()
        }
    }

    // Handle gallery result
    private fun handleGalleryResult(data: Intent?) {
        val uri = data?.data
        if (uri != null) {
            try {
                selectedImageUri = uri
                register_IMG_profile.setImageURI(uri)
                Log.d(TAG, "Gallery image URI: $uri")
            } catch (e: Exception) {
                Log.e(TAG, "Error processing gallery image: ${e.message}", e)
                Toast.makeText(this, "שגיאה בעיבוד תמונה מהגלריה", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.e(TAG, "Gallery returned null URI")
            Toast.makeText(this, "לא נבחרה תמונה מהגלריה", Toast.LENGTH_SHORT).show()
        }
    }

    // Show rationale dialog for permissions
    private fun showPermissionRationale(permissionType: PermissionType) {
        val title: String
        val message: String

        if (permissionType == PermissionType.CAMERA) {
            title = "דרושה הרשאה למצלמה"
            message = "כדי לצלם תמונת פרופיל, האפליקציה צריכה הרשאה לגשת למצלמה."
        } else {
            title = "דרושה הרשאה לגלריה"
            message = "כדי לבחור תמונת פרופיל, האפליקציה צריכה הרשאה לגשת לגלריה."
        }

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("אישור") { _, _ ->
                if (permissionType == PermissionType.CAMERA) {
                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                } else {
                    val permission = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        android.Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                    galleryPermissionLauncher.launch(permission)
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    // Convert bitmap to URI
    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        try {
            // Create a unique filename with timestamp
            val timestamp = System.currentTimeMillis()
            val fileName = "profile_image_$timestamp.jpg"

            // Get cache directory file
            val cachePath = File(applicationContext.cacheDir, "images")
            cachePath.mkdirs() // Make sure directory exists

            val file = File(cachePath, fileName)

            // Write bitmap to file
            FileOutputStream(file).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }

            // Return URI using FileProvider
            return FileProvider.getUriForFile(
                applicationContext,
                "${applicationContext.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error creating image from bitmap: ${e.message}", e)
            throw e
        }
    }

    // Register the user
    private fun handleRegistration() {
        val firstName = register_ET_firstname.text.toString().trim()
        val lastName = register_ET_lastname.text.toString().trim()
        val email = register_ET_email.text.toString().trim()
        val password = register_ET_password.text.toString()

        try {
            usersManager.addNewUser(firstName, lastName, email, password, selectedImageUri)
        } catch (e: Exception) {
            Log.e(TAG, "Error in registration: ${e.message}", e)
            Toast.makeText(this, "שגיאה ברישום: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}