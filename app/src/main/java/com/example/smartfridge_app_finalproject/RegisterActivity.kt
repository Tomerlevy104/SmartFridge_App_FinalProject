package com.example.smartfridge_app_finalproject

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.FileProvider
import com.example.smartfridge_app_finalproject.managers.UploadImageManager
import com.example.smartfridge_app_finalproject.managers.UsersManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.google.android.material.imageview.ShapeableImageView
import java.io.File
import java.io.FileOutputStream
import android.util.Log

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
    private var selectedImageUri: Uri? = null
    private var cameraImageUri: Uri? = null

    private lateinit var register_IMG_profile: ShapeableImageView
    private lateinit var register_BTN_camera: MaterialButton
    private lateinit var register_BTN_gallery: MaterialButton
    private lateinit var register_BTN_submit: MaterialButton

    private lateinit var uploadImageManager: UploadImageManager

    private val validInputManager = ValidInputManager.getInstance() //Valid input manager
    private lateinit var usersManager: UsersManager
    private val TAG = "RegisterActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        usersManager = UsersManager(this)
        uploadImageManager = UploadImageManager()
        findViews()
        initViews()
    }

    private fun findViews() {
        //TextInputLayouts
        register_TIL_firstname = findViewById(R.id.register_TIL_firstname)
        register_TIL_lastname = findViewById(R.id.register_TIL_lastname)
        register_TIL_email = findViewById(R.id.register_TIL_email)
        register_TIL_password = findViewById(R.id.register_TIL_password)
        register_TIL_confirmpassword = findViewById(R.id.register_TIL_confirmpassword)

        //EditTexts
        register_ET_firstname = findViewById(R.id.register_ET_firstname)
        register_ET_lastname = findViewById(R.id.register_ET_lastname)
        register_ET_email = findViewById(R.id.register_ET_email)
        register_ET_password = findViewById(R.id.register_ET_password)
        register_ET_confirmpassword = findViewById(R.id.register_ET_confirmpassword)

        // Button
        register_IMG_profile = findViewById(R.id.register_IMG_profile)
        register_BTN_camera = findViewById(R.id.register_BTN_camera)
        register_BTN_gallery = findViewById(R.id.register_BTN_gallery)
        register_BTN_submit = findViewById(R.id.register_BTN_submit)
    }

    private fun initViews() {
        setupTextChangeListeners()

        register_BTN_submit.setOnClickListener {
            if (validateForm()) {
                handleRegistration()
            }
        }

        register_BTN_camera.setOnClickListener {
            if (uploadImageManager.checkCameraPermission(this)) {
                openCamera()
            } else {
                if (uploadImageManager.shouldShowRequestPermissionRationale(this, PermissionType.CAMERA)) {
                    showPermissionRationale(PermissionType.CAMERA)
                } else {
                    uploadImageManager.requestCameraPermission(this)
                }
            }
        }

        register_BTN_gallery.setOnClickListener {
            if (uploadImageManager.checkGalleryPermission(this)) {
                openGallery()
            } else {
                if (uploadImageManager.shouldShowRequestPermissionRationale(this, PermissionType.GALLERY)) {
                    showPermissionRationale(PermissionType.GALLERY)
                } else {
                    uploadImageManager.requestGalleryPermission(this)
                }
            }
        }
    }

    private fun setupTextChangeListeners() {
        val fieldPairs = mapOf(
            register_ET_firstname to register_TIL_firstname,
            register_ET_lastname to register_TIL_lastname,
            register_ET_email to register_TIL_email,
            register_ET_password to register_TIL_password,
            register_ET_confirmpassword to register_TIL_confirmpassword
        )

        fieldPairs.forEach { (editText, inputLayout) ->
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

    // פתיחת מצלמה
    private fun openCamera() {
        try {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Create a file to save the image
            val photoFile = File(applicationContext.cacheDir, "temp_camera_image.jpg")
            cameraImageUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )

            // Tell the camera where to save the full-size image
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(intent, Constants.ImageUploadRequest.CAMERA_REQUEST_CODE)
            } else {
                Toast.makeText(this, "אין אפליקציית מצלמה זמינה", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}", e)
            Toast.makeText(this, "שגיאה בפתיחת המצלמה", Toast.LENGTH_SHORT).show()
        }
    }

    // פתיחת גלריה
    private fun openGallery() {
        try {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constants.ImageUploadRequest.GALLERY_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}", e)
            Toast.makeText(this, "שגיאה בפתיחת הגלריה", Toast.LENGTH_SHORT).show()
        }
    }

    // הצגת הסבר על הרשאות
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
                    uploadImageManager.requestCameraPermission(this)
                } else {
                    uploadImageManager.requestGalleryPermission(this)
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    // טיפול בתוצאות מהמצלמה או הגלריה
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (resultCode == Activity.RESULT_OK) {
                when (requestCode) {
                    Constants.ImageUploadRequest.CAMERA_REQUEST_CODE -> {
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
                    // Gallery code remains unchanged
                    Constants.ImageUploadRequest.GALLERY_REQUEST_CODE -> {
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
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onActivityResult: ${e.message}", e)
            Toast.makeText(this, "שגיאה בעיבוד התמונה", Toast.LENGTH_SHORT).show()
        }
    }

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
            // Instead of rethrowing, return a default URI or null
            throw e
        }
    }

    // טיפול בתוצאות בקשת הרשאות
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            Constants.ImageUploadRequest.CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "הרשאת מצלמה נדרשת לצילום תמונת פרופיל", Toast.LENGTH_SHORT).show()
                }
            }
            Constants.ImageUploadRequest.GALLERY_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "הרשאת גלריה נדרשת לבחירת תמונת פרופיל", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

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