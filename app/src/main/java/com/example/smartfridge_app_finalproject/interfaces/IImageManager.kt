package com.example.smartfridge_app_finalproject.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher

/**
 * Interface for managing image-related operations in the application.
 */
interface IImageManager {

    /**
     * Uploads a profile image to remote storage
     * @param context Context used for accessing resources and services
     * @param imageUri URI of the image to upload
     * @param callback Function that receives the result of the operation (success/failure and download URL)
     */
    suspend fun uploadProfileImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit)

    /**
     * Uploads a product image to remote storage
     * @param context Context used for accessing resources and services
     * @param imageUri URI of the image to upload
     * @param callback Function that receives the result of the operation (success/failure and download URL)
     */
    suspend fun uploadProductImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit)

    /**
     * Launches the device camera to take a picture
     * @param activity Activity context for launching the camera
     * @param callback ActivityResultLauncher that will handle the result of the camera operation
     */
    fun takePicture(activity: Activity, callback: ActivityResultLauncher<Intent>)

    /**
     * Launches the gallery picker to select an image
     * @param activity Activity context for launching the gallery
     * @param callback ActivityResultLauncher that will handle the result of the gallery selection
     */
    fun pickFromGallery(activity: Activity, callback: ActivityResultLauncher<Intent>)

    /**
     * Checks and requests camera permission if needed
     * @param activity Activity context for permission requests
     * @param callback ActivityResultLauncher that will handle the permission request result
     */
    fun checkAndRequestCameraPermission(activity: Activity, callback: ActivityResultLauncher<String>)

    /**
     * Checks and requests gallery permission if needed
     * @param activity Activity context for permission requests
     * @param callback ActivityResultLauncher that will handle the permission request result
     */
    fun checkAndRequestGalleryPermission(activity: Activity, callback: ActivityResultLauncher<String>)
}