package com.example.smartfridge_app_finalproject.managers

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import android.content.Context
import com.example.smartfridge_app_finalproject.interfaces.IImageManager
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import java.util.UUID

class UploadImageManager : IImageManager {
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val storageRef: StorageReference = storage.reference

    companion object {
        private const val CAMERA_PERMISSION = Manifest.permission.CAMERA
        private const val PROFILE_IMAGES_PATH = "profile_images"
        private const val PRODUCT_IMAGES_PATH = "products_images"
        private val GALLERY_PERMISSION = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    override fun uploadProfileImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        Log.d("UploadImageManager", "Starting upload for URI: $imageUri")

        // Generate a unique filename for the image
        val filename = "${UUID.randomUUID()}.jpg"
        val profileImagesRef = storageRef.child("$PROFILE_IMAGES_PATH/$filename")

        // Start upload task
        profileImagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                Log.d("UploadImageManager", "Upload successful, getting download URL")
                // Get the download URL
                profileImagesRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        Log.d("UploadImageManager", "Download URL obtained: $downloadUri")
                        // Return success with the download URL
                        callback(true, downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        Log.e("UploadImageManager", "Failed to get download URL", exception)
                        // Handle failure to get download URL
                        callback(false, "Failed to get download URL: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("UploadImageManager", "Upload failed", exception)
                // Handle upload failure
                callback(false, "Upload failed: ${exception.message}")
            }
            .addOnProgressListener { taskSnapshot ->
                // Calculate upload progress
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("UploadImageManager", "Upload progress: $progress%")
            }
    }

    override fun uploadProductImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit) {
        val filename = "${UUID.randomUUID()}.jpg"
        val productImagesRef = storageRef.child("$PRODUCT_IMAGES_PATH/$filename")

        productImagesRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                productImagesRef.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        callback(true, downloadUri.toString())
                    }
                    .addOnFailureListener { exception ->
                        callback(false, "Failed to get download URL: ${exception.message}")
                    }
            }
            .addOnFailureListener { exception ->
                callback(false, "Upload failed: ${exception.message}")
            }
    }

    override fun takePicture(activity: Activity, callback: ActivityResultLauncher<Intent>) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        callback.launch(intent)
    }

    override fun pickFromGallery(activity: Activity, callback: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        callback.launch(intent)
    }

    override fun checkAndRequestCameraPermission(activity: Activity, callback: ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                CAMERA_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED -> {
                takePicture(activity, callback as ActivityResultLauncher<Intent>)
            }
            else -> {
                callback.launch(CAMERA_PERMISSION)
            }
        }
    }

    override fun checkAndRequestGalleryPermission(activity: Activity, callback: ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(
                activity,
                GALLERY_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED -> {
                pickFromGallery(activity, callback as ActivityResultLauncher<Intent>)
            }
            else -> {
                callback.launch(GALLERY_PERMISSION)
            }
        }
    }
}