package com.example.smartfridge_app_finalproject.fragments

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.StartingPageActivity
import com.example.smartfridge_app_finalproject.adapters.UserAdapter
import com.example.smartfridge_app_finalproject.data.model.User
import com.example.smartfridge_app_finalproject.data.repository.UsersRepository
import com.example.smartfridge_app_finalproject.databinding.FragmentManageProfileBinding
import com.example.smartfridge_app_finalproject.managers.UploadImageManager
import com.example.smartfridge_app_finalproject.utilities.Constants
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class ManageProfileFragment : Fragment() {

    private lateinit var binding: FragmentManageProfileBinding
    private lateinit var userAdapter: UserAdapter
    private var usersRepository = UsersRepository()
    private val uploadImageManager = UploadImageManager()

    // ניהול תוצאות פעילויות
    private var tempUri: Uri? = null

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showToast("נדרשת הרשאת מצלמה לצילום תמונת פרופיל")
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openGallery()
        } else {
            showToast("נדרשת הרשאת גלריה לבחירת תמונת פרופיל")
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { handleSelectedImage(it) }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempUri?.let { handleSelectedImage(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        setupLogoutButton()
        loadInitialUsers("benny_l")
    }

    private fun setupClickListeners() {
        binding.profileManagementIMGUserProfile.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun setupLogoutButton() {
        binding.profileManagementBTNLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("התנתקות")
            .setMessage("האם אתה בטוח שברצונך להתנתק?")
            .setNegativeButton("לא") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("כן") { _, _ ->
                signOut()
            }
            .show()
    }

    private fun signOut() {
        AuthUI.getInstance()
            .signOut(requireContext())
            .addOnCompleteListener {
                startActivity(Intent(requireContext(), StartingPageActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                requireActivity().finish()
            }
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("בחר תמונת פרופיל")
            .setItems(arrayOf("מצלמה", "גלריה")) { _, which ->
                when (which) {
                    0 -> handleCameraRequest()
                    1 -> handleGalleryRequest()
                }
            }
            .show()
    }

    private fun handleCameraRequest() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showPermissionExplanationDialog(PermissionType.CAMERA)
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun handleGalleryRequest() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                openGallery()
            }
            shouldShowRequestPermissionRationale(permission) -> {
                showPermissionExplanationDialog(PermissionType.GALLERY)
            }
            else -> {
                galleryPermissionLauncher.launch(permission)
            }
        }
    }

    private fun showPermissionExplanationDialog(permissionType: PermissionType) {
        val message = when (permissionType) {
            PermissionType.CAMERA -> "אנחנו צריכים גישה למצלמה כדי לצלם תמונת פרופיל"
            PermissionType.GALLERY -> "אנחנו צריכים גישה לגלריה כדי לבחור תמונת פרופיל"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("נדרשת הרשאה")
            .setMessage(message)
            .setPositiveButton("אישור") { _, _ ->
                when (permissionType) {
                    PermissionType.CAMERA -> cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    PermissionType.GALLERY -> {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        galleryPermissionLauncher.launch(permission)
                    }
                }
            }
            .setNegativeButton("ביטול", null)
            .show()
    }

    private fun openCamera() {
        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "Temp Picture")
                put(MediaStore.Images.Media.DESCRIPTION, "Temp Description")
            }

            tempUri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )

            tempUri?.let { uri ->
                cameraLauncher.launch(uri)
            } ?: run {
                showToast("שגיאה ביצירת קובץ זמני למצלמה")
            }
        } catch (e: Exception) {
            Log.e("ManageProfileFragment", "Error opening camera: ${e.message}")
            showToast("שגיאה בפתיחת המצלמה")
        }
    }

    private fun openGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e("ManageProfileFragment", "Error opening gallery: ${e.message}")
            showToast("שגיאה בפתיחת הגלריה")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                showLoading(true)

                // מחיקת תמונה ישנה
                uploadImageManager.deleteOldProfileImage(userId)

                // העלאת תמונה חדשה
                val result = uploadImageManager.uploadProfileImage(userId, uri, requireContext())

                result.onSuccess { url ->
                    // עדכון תמונת הפרופיל
                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.profileManagementIMGUserProfile)

                    // טעינת נתוני המשתמש המעודכנים
                    loadInitialUsers(userId)

                    showToast("התמונה הועלתה בהצלחה")
                }.onFailure { exception ->
                    showToast("שגיאה בהעלאת התמונה: ${exception.message}")
                }
            } catch (e: Exception) {
                showToast("שגיאה: ${e.message}")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(mutableListOf()) { user, isActive ->
            // Handle user status change
        }

        binding.profileManagementRVUsers.apply {
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadInitialUsers(username: String) {
        // הצגת טעינה
        showLoading(true)

        // שליפת המשתמש מ-Firestore
        FirebaseAuth.getInstance().currentUser?.uid?.let { userId ->
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    document.toObject(User::class.java)?.let { user ->
                        // עדכון ה-UI עם המשתמש מ-Firestore
                        userAdapter.updateUsers(listOf(user))

                        // עדכון תמונת הפרופיל הראשית
                        if (!user.profileImageUrl.isNullOrEmpty()) {
                            Glide.with(requireContext())
                                .load(user.profileImageUrl)
                                .placeholder(R.drawable.profile_man)
                                .error(R.drawable.profile_man)
                                .into(binding.profileManagementIMGUserProfile)
                        }
                    }
                    showLoading(false)
                }
                .addOnFailureListener { e ->
                    showToast("שגיאה בטעינת נתוני משתמש: ${e.message}")
                    showLoading(false)
                }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
//        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
}