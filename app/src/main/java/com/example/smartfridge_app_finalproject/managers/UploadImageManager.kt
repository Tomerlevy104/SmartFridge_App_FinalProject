package com.example.smartfridge_app_finalproject.managers

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.smartfridge_app_finalproject.data.model.User
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UploadImageManager {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    ///////////////////////////////////////////////////////////////////////////////////

    //Permissions

    fun checkCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA),
            Constants.ImageUploadRequest.CAMERA_PERMISSION_REQUEST
        )
    }

    fun checkGalleryPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_MEDIA_VIDEO
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestGalleryPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO
            ),
            Constants.ImageUploadRequest.GALLERY_PERMISSION_REQUEST
        )
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, permissionType: PermissionType): Boolean {
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
                    Manifest.permission.READ_MEDIA_IMAGES
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.READ_MEDIA_VIDEO
                )
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////

    //Upload to Firebase

    suspend fun uploadProfileImage(
        userId: String,
        imageUri: Uri,
        context: Context
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val imageUrl = uploadImageToStorage(userId, imageUri)
            updateUserProfileImage(userId, imageUrl)
            Result.success(imageUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadImageToStorage(userId: String, imageUri: Uri): String {
        val storageRef = storage.reference
            .child("profile_images")
            .child(userId)
            .child("profile_${System.currentTimeMillis()}.jpg")

        // העלאת התמונה וקבלת ה-URL
        val uploadTask = storageRef.putFile(imageUri)
        return uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.await().toString()
    }

    private suspend fun updateUserProfileImage(userId: String, imageUrl: String) {
        firestore.collection("users")
            .document(userId)
            .update("profileImageUrl", imageUrl)
            .await()
    }

    suspend fun deleteOldProfileImage(userId: String) {
        try {
            val user = firestore.collection("users")
                .document(userId)
                .get()
                .await()
                .toObject(User::class.java)

            user?.profileImageUri?.let { uri ->
                if (uri== null) {
                    storage.getReferenceFromUrl(uri).delete().await()
                }
            }
        } catch (e: Exception) {
            // טיפול בשגיאות
        }
    }
}