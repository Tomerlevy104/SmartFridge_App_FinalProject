package com.example.smartfridge_app_finalproject.interfaces

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher

interface IImageManager {
    fun uploadProfileImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit)
    fun uploadProductImage(context: Context, imageUri: Uri, callback: (Boolean, String?) -> Unit)
    fun takePicture(activity: Activity, callback: ActivityResultLauncher<Intent>)
    fun pickFromGallery(activity: Activity, callback: ActivityResultLauncher<Intent>)
    fun checkAndRequestCameraPermission(activity: Activity, callback: ActivityResultLauncher<String>)
    fun checkAndRequestGalleryPermission(activity: Activity, callback: ActivityResultLauncher<String>)
}