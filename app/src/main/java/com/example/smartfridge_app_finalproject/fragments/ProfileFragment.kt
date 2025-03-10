package com.example.smartfridge_app_finalproject.fragments

import UserHandler
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.StartingPageActivity
import com.example.smartfridge_app_finalproject.databinding.FragmentManageProfileBinding
import com.example.smartfridge_app_finalproject.managers.UploadImageManager
import com.example.smartfridge_app_finalproject.managers.ValidInputManager
import com.example.smartfridge_app_finalproject.utilities.PermissionType
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentManageProfileBinding
    private lateinit var uploadImageManager: UploadImageManager
    private lateinit var userHandler: UserHandler
    private val TAG = "ProfileFragment"

    // Uri to store temporary camera image
    private var tempCameraUri: Uri? = null

    // ActivityResultLaunchers for permissions and image operations
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            showToast("נדרשת הרשאת מצלמה לצילום תמונת פרופיל")
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchGallery()
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
            tempCameraUri?.let { handleSelectedImage(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentManageProfileBinding.inflate(inflater, container, false)
        uploadImageManager = UploadImageManager.getInstance()
        userHandler = UserHandler.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        setupLogoutButton()
        setupEditButtons()
        loadUserProfile()
    }

    private fun setupClickListeners() {
        // Profile image change
        binding.profileBTNChangeImage.setOnClickListener {
            showImagePickerDialog()
        }

        binding.profileManagementIMGUserProfile.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun setupLogoutButton() {
        binding.profileManagementBTNLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun setupEditButtons() {
        // First name edit
        binding.profileBTNEditFirstName.setOnClickListener {
            showEditField(
                binding.profileTILFirstName,
                binding.profileLLFirstNameButtons,
                binding.profileTVFirstName.text.toString()
            )
        }

        // Last name edit
        binding.profileBTNEditLastName.setOnClickListener {
            showEditField(
                binding.profileTILLastName,
                binding.profileLLLastNameButtons,
                binding.profileTVLastName.text.toString()
            )
        }

        // First name save
        binding.profileBTNSaveFirstName.setOnClickListener {
            val newFirstName = binding.profileETFirstName.text.toString().trim()
            if (newFirstName.isNotEmpty()) {
                // Process update - only hide fields if update is successful
                val validInputManager = ValidInputManager.getInstance()
                if (validInputManager.isValidFirstName(newFirstName)) {
                    updateUserField("firstName", newFirstName)
                    binding.profileTVFirstName.text = newFirstName
                    hideEditField(
                        binding.profileTILFirstName,
                        binding.profileLLFirstNameButtons
                    )
                } else {
                    binding.profileETFirstName.error = getString(R.string.invalid_first_name)
                    showToast(getString(R.string.invalid_first_name))
                }
            } else {
                binding.profileETFirstName.error = "השם אינו יכול להיות ריק"
                showToast("השם אינו יכול להיות ריק")
            }
        }

        // Last name save
        binding.profileBTNSaveLastName.setOnClickListener {
            val newLastName = binding.profileETLastName.text.toString().trim()
            if (newLastName.isNotEmpty()) {
                // Process update - only hide fields if update is successful
                val validInputManager = ValidInputManager.getInstance()
                if (validInputManager.isValidLastName(newLastName)) {
                    updateUserField("lastName", newLastName)
                    binding.profileTVLastName.text = newLastName
                    hideEditField(
                        binding.profileTILLastName,
                        binding.profileLLLastNameButtons
                    )
                } else {
                    binding.profileETLastName.error = getString(R.string.invalid_last_name)
                    showToast(getString(R.string.invalid_last_name))
                }
            } else {
                binding.profileETLastName.error = "שם המשפחה אינו יכול להיות ריק"
                showToast("שם המשפחה אינו יכול להיות ריק")
            }
        }

        // Cancel buttons
        binding.profileBTNCancelFirstName.setOnClickListener {
            hideEditField(
                binding.profileTILFirstName,
                binding.profileLLFirstNameButtons
            )
        }

        binding.profileBTNCancelLastName.setOnClickListener {
            hideEditField(
                binding.profileTILLastName,
                binding.profileLLLastNameButtons
            )
        }

        // Reset password button
        binding.profileBTNResetPassword.setOnClickListener {
            resetPassword()
        }
    }

    private fun showEditField(inputLayout: View, buttonsLayout: View, currentValue: String) {
        // Show the edit field and set current value
        inputLayout.visibility = View.VISIBLE
        buttonsLayout.visibility = View.VISIBLE

        // Set current value
        when (inputLayout.id) {
            R.id.profile_TIL_firstName -> binding.profileETFirstName.setText(currentValue)
            R.id.profile_TIL_lastName -> binding.profileETLastName.setText(currentValue)
        }
    }

    private fun hideEditField(inputLayout: View, buttonsLayout: View) {
        inputLayout.visibility = View.GONE
        buttonsLayout.visibility = View.GONE
    }

    private fun updateUserField(fieldName: String, value: String) {
        // Validate the input using ValidInputManager
        val validInputManager = ValidInputManager.getInstance()

        when (fieldName) {
            "firstName" -> {
                if (!validInputManager.isValidFirstName(value)) {
                    activity?.runOnUiThread {
                        binding.profileETFirstName.error = getString(R.string.invalid_first_name)
                    }
                    return
                }
            }
            "lastName" -> {
                if (!validInputManager.isValidLastName(value)) {
                    activity?.runOnUiThread {
                        binding.profileETLastName.error = getString(R.string.invalid_last_name)
                    }
                    return
                }
            }
        }

        userHandler.getCurrentUserData()?.let { userData ->
            val firstName = if (fieldName == "firstName") value else userData.firstName
            val lastName = if (fieldName == "lastName") value else userData.lastName

            userHandler.updateUserProfile(
                firstName = firstName,
                lastName = lastName,
                profileImageUrl = userData.profileImageUrl
            ) { result ->
                activity?.runOnUiThread {
                    result.onSuccess {
                        showToast("הפרטים עודכנו בהצלחה")
                    }.onFailure { exception ->
                        showToast("שגיאה בעדכון הפרטים: ${exception.message}")
                    }
                }
            }
        }
    }

    private fun resetPassword() {
        userHandler.getCurrentUserData()?.let { userData ->
            userData.email?.let { email ->
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnSuccessListener {
                        showToast("הוראות לאיפוס סיסמה נשלחו לכתובת האימייל שלך")
                    }
                    .addOnFailureListener { exception ->
                        showToast("שגיאה בשליחת הוראות איפוס: ${exception.message}")
                    }
            } ?: showToast("לא נמצאה כתובת אימייל")
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
        userHandler.clearUserData() // Clear cached user data

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
        if (uploadImageManager.checkCameraPermission(requireContext())) {
            launchCamera()
        } else {
            if (uploadImageManager.shouldShowRequestPermissionRationale(requireActivity(), PermissionType.CAMERA)) {
                showPermissionExplanationDialog(PermissionType.CAMERA)
            } else {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun handleGalleryRequest() {
        if (uploadImageManager.checkGalleryPermission(requireContext())) {
            launchGallery()
        } else {
            if (uploadImageManager.shouldShowRequestPermissionRationale(requireActivity(), PermissionType.GALLERY)) {
                showPermissionExplanationDialog(PermissionType.GALLERY)
            } else {
                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
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

    private fun launchCamera() {
        try {
            val (intent, uri) = uploadImageManager.createCameraIntent(requireContext())
            tempCameraUri = uri

            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera: ${e.message}", e)
            showToast("שגיאה בפתיחת המצלמה")
        }
    }

    private fun launchGallery() {
        try {
            galleryLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Error opening gallery: ${e.message}", e)
            showToast("שגיאה בפתיחת הגלריה")
        }
    }

    private fun handleSelectedImage(uri: Uri) {
        lifecycleScope.launch {
            try {
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                showLoading(true)

                // Delete old profile image first
                uploadImageManager.deleteOldProfileImage(userId)

                // Use the new method to upload profile image
                uploadImageManager.uploadProfileImage(requireContext(), uri) { success, imageUrl ->
                    if (success) {
                        // Update profile image in UI
                        Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.profile_man)
                            .error(R.drawable.profile_man)
                            .into(binding.profileManagementIMGUserProfile)

                        // Reload user data to get updated profile
                        loadUserProfile()

                        showToast("התמונה הועלתה בהצלחה")
                    } else {
                        showToast("שגיאה בהעלאת התמונה: $imageUrl")
                    }

                    showLoading(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error handling selected image", e)
                showToast("שגיאה: ${e.message}")
                showLoading(false)
            }
        }
    }

    private fun loadUserProfile() {
        showLoading(true)

        // First check if we have cached user data
        userHandler.getCurrentUserData()?.let { userData ->
            updateProfileUI(userData)
        }

        // Then load/refresh from Firestore
        userHandler.loadUserProfile { result ->
            activity?.runOnUiThread {
                result.onSuccess { userData ->
                    updateProfileUI(userData)
                }.onFailure { exception ->
                    Log.e(TAG, "Error loading user profile", exception)
                    showToast("שגיאה בטעינת נתוני משתמש: ${exception.message}")
                }
                showLoading(false)
            }
        }
    }

    private fun updateProfileUI(userData: UserHandler.UserData) {
        try {
            // Update profile image if available
            if (!userData.profileImageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(userData.profileImageUrl)
                    .placeholder(R.drawable.profile_man)
                    .error(R.drawable.profile_man)
                    .into(binding.profileManagementIMGUserProfile)
            }

            // Update text fields with user data
            binding.profileTVFirstName.text = userData.firstName
            binding.profileTVLastName.text = userData.lastName
//            binding.profileTVEmail.text = userData.email ?: getString(R.string.no_email_available)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile UI", e)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(show: Boolean) {
        try {
            binding.profileProgressBar.visibility = if (show) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            Log.e(TAG, "Error showing/hiding loading indicator", e)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data every time the fragment is resumed
        loadUserProfile()
    }
}