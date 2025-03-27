package com.example.smartfridge_app_finalproject.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.smartfridge_app_finalproject.interfaces.IImageManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manager class for handling all image-related operations including:
 * - Permission handling for camera and gallery
 * - Taking pictures or selecting from gallery
 * - Uploading images to Firebase Storage
 * - Linking uploaded images to appropriate collections in firestore
 */
class UploadImageManager private constructor() : IImageManager {

    private val TAG = "UploadImageManager"
    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        @Volatile
        private var instance: UploadImageManager? = null

        fun getInstance(): UploadImageManager {
            return instance ?: synchronized(this) {
                instance ?: UploadImageManager().also { instance = it }
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Checks if camera permission is granted
    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Requests camera permission
    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            Constants.ImageUploadRequest.CAMERA_PERMISSION_REQUEST
        )
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Checks if gallery permission is granted
    fun checkGalleryPermission(context: Context): Boolean {
        // Use the appropriate permission based on Android version
        val permission = getRequiredGalleryPermission()

        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Returns the appropriate gallery permission based on Android version
    fun getRequiredGalleryPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Requests gallery permission
    fun requestGalleryPermission(activity: Activity) {
        val permissions = arrayOf(getRequiredGalleryPermission())

        ActivityCompat.requestPermissions(
            activity,
            permissions,
            Constants.ImageUploadRequest.GALLERY_PERMISSION_REQUEST
        )
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Checks if rationale should be shown for a specific permission type
    fun shouldShowRequestPermissionRationale(
        activity: Activity,
        permissionType: PermissionType
    ): Boolean {
        return when (permissionType) {
            PermissionType.CAMERA -> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CAMERA
                )
            }

            PermissionType.GALLERY -> {
                ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    getRequiredGalleryPermission()
                )
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Creates a URI for storing a captured image
    // return URI where the image will be saved
    fun createImageCaptureUri(context: Context): Uri? {
        try {
            // Create a unique file name
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"

            // Get the app's private directory for storing images
            val storageDir = File(context.cacheDir, "camera_photos")
            storageDir.mkdirs()

            // Create the temporary file
            val imageFile = File.createTempFile(imageFileName, ".jpg", storageDir)

            // Return the file's URI through FileProvider
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: IOException) {
            Log.e(TAG, "Error creating image file: ${e.message}", e)
            return null
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Creates and configures an intent to take a picture using the camera
    // return Pair containing the Intent and the Uri where the image will be saved
    fun createCameraIntent(context: Context): Pair<Intent, Uri?> {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Check if device has camera
        if (intent.resolveActivity(context.packageManager) == null) {
            Log.e(TAG, "No camera app available")
            throw IOException("No camera app available on device")
        }

        // Create a URI for the image
        val photoUri = createImageCaptureUri(context)

        // If URI was created successfully, continue
        if (photoUri != null) {
            // Set output URI and grant permissions
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }

        return Pair(intent, photoUri)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Creates an intent to select an image from gallery
    fun createGalleryIntent(): Intent {
        return Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Implementation of interface methods
    override fun takePicture(activity: Activity, callback: ActivityResultLauncher<Intent>) {
        try {
            val (intent, _) = createCameraIntent(activity)
            callback.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching camera", e)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun pickFromGallery(activity: Activity, callback: ActivityResultLauncher<Intent>) {
        try {
            val intent = createGalleryIntent()
            callback.launch(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching gallery", e)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun checkAndRequestCameraPermission(
        activity: Activity,
        callback: ActivityResultLauncher<String>
    ) {
        if (!checkCameraPermission(activity)) {
            callback.launch(Manifest.permission.CAMERA)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    override fun checkAndRequestGalleryPermission(
        activity: Activity,
        callback: ActivityResultLauncher<String>
    ) {
        if (!checkGalleryPermission(activity)) {
            callback.launch(getRequiredGalleryPermission())
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Uploads a profile image to Firebase Storage and updates user profile
    // return Result with the image URL on success, or an exception on failure
    override suspend fun uploadProfileImage(
        context: Context,
        imageUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Upload to Firebase Storage
            val storageRef = storage.reference
                .child("profile_images")
                .child(userId)
                .child("profile_${System.currentTimeMillis()}.jpg")

            val uploadTask = storageRef.putFile(imageUri)
            val downloadUrl = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.await().toString()

            // Update user profile in firestore
            firestore.collection("users")
                .document(userId)
                .update("profileImageUrl", downloadUrl)
                .await()

            callback(true, downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading profile image", e)
            callback(false, e.message)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Uploads a product image to Firebase Storage and updates product document
    // return Result with the image URL on success, or an exception on failure
    override suspend fun uploadProductImage(
        context: Context,
        imageUri: Uri,
        callback: (Boolean, String?) -> Unit
    ) {
        try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

            // Extract product ID from the context or parameters if needed
            // For this example, we'll use a timestamp as a placeholder
            val productId = System.currentTimeMillis().toString()

            // Upload to Firebase Storage
            val storageRef = storage.reference
                .child("product_images")
                .child(userId)
                .child("product_${productId}.jpg")

            val uploadTask = storageRef.putFile(imageUri)
            val downloadUrl = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.await().toString()

            // Note: Actual implementation would update the specific product document
            // with the new image URL in firestore
            callback(true, downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading product image", e)
            callback(false, e.message)
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Uploads an image to Firebase Storage
     * @param path The storage path where the image should be stored
     * @param fileName The name for the image file
     * @param imageUri The Uri of the image to upload
     * @return The download URL of the uploaded image
     */
    suspend fun uploadImageToStorage(path: String, fileName: String, imageUri: Uri): String =
        withContext(Dispatchers.IO) {
            val storageRef = storage.reference
                .child(path)
                .child(fileName)

            val uploadTask = storageRef.putFile(imageUri)
            return@withContext uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                storageRef.downloadUrl
            }.await().toString()
        }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Updates a user's profile image URL in firestore
    suspend fun updateUserProfileImage(userId: String, imageUrl: String) =
        withContext(Dispatchers.IO) {
            firestore.collection("users")
                .document(userId)
                .update("profileImageUrl", imageUrl)
                .await()
        }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Updates a product's image URL in firestore
    suspend fun updateProductImage(userId: String, productId: String, imageUrl: String) =
        withContext(Dispatchers.IO) {
            firestore.collection("users")
                .document(userId)
                .collection("products")
                .document(productId)
                .update("imageUrl", imageUrl)
                .await()
        }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Deletes an old profile image from Firebase Storage
    suspend fun deleteOldProfileImage(userId: String) = withContext(Dispatchers.IO) {
        try {
            val userDoc = firestore.collection("users")
                .document(userId)
                .get()
                .await()

            val profileImageUrl = userDoc.getString("profileImageUrl")

            if (!profileImageUrl.isNullOrEmpty()) {
                // Extract the path from the URL and delete the file
                val httpsReference = storage.getReferenceFromUrl(profileImageUrl)
                httpsReference.delete().await()
            } else {

            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting old profile image", e)
            // Continue without throwing, since this is a cleanup operation
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////
    // Deletes an old product image from Firebase Storage
    suspend fun deleteOldProductImage(userId: String, productId: String) =
        withContext(Dispatchers.IO) {
            try {
                val productDoc = firestore.collection("users")
                    .document(userId)
                    .collection("products")
                    .document(productId)
                    .get()
                    .await()

                val imageUrl = productDoc.getString("imageUrl")

                if (!imageUrl.isNullOrEmpty()) {
                    // Extract the path from the URL and delete the file
                    val httpsReference = storage.getReferenceFromUrl(imageUrl)
                    httpsReference.delete().await()
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting old product image", e)
                // Continue without throwing, since this is a cleanup operation
            }
        }
    //////////////////////////////////////////////////////////////////////////////////////////////////////
}