package com.example.smartfridge_app_finalproject.fragments

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartfridge_app_finalproject.StartingPageActivity
import com.example.smartfridge_app_finalproject.adapters.UserAdapter
import com.example.smartfridge_app_finalproject.data.repository.UsersRepository
import com.example.smartfridge_app_finalproject.databinding.FragmentManageProfileBinding
import com.example.smartfridge_app_finalproject.interfaces.IImageManager
import com.example.smartfridge_app_finalproject.managers.UploadImageManager
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.ByteArrayOutputStream

class ManageProfileFragment : Fragment() {

    private lateinit var binding: FragmentManageProfileBinding
    private lateinit var userAdapter: UserAdapter
    private var usersRepository = UsersRepository()
    private val uploadImageManager: IImageManager = UploadImageManager()

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uploadImageManager.takePicture(requireActivity(), cameraLauncher)
        } else {
            Toast.makeText(context, "נדרשת הרשאת מצלמה", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            uploadImageManager.pickFromGallery(requireActivity(), galleryLauncher)
        } else {
            Toast.makeText(context, "נדרשת הרשאת גלריה", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.extras?.get("data")?.let { image ->
                val bitmap = image as Bitmap
                binding.profileManagementIMGUserProfile.setImageBitmap(bitmap)

                // Convert bitmap to URI for the uploadImageManager
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val path = MediaStore.Images.Media.insertImage(
                    requireContext().contentResolver,
                    bitmap,
                    "ProfileImage_${System.currentTimeMillis()}",
                    null
                )
                val imageUri = Uri.parse(path)

                // Use uploadImageManager for consistency
                uploadImageManager.uploadProfileImage(requireContext(), imageUri) { success, message ->
                    if (success) {
                        Toast.makeText(context, "התמונה הועלתה בהצלחה", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, message ?: "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                binding.profileManagementIMGUserProfile.setImageURI(uri)
                uploadImageManager.uploadProfileImage(requireContext(), uri) { success, message ->
                    if (success) {
                        Toast.makeText(context, "התמונה הועלתה בהצלחה", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, message ?: "שגיאה בהעלאת התמונה", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
                // מעבר ל-StartingPageActivity
                val intent = Intent(requireContext(), StartingPageActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                requireActivity().finish()
            }
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("בחר תמונת פרופיל")
            .setItems(arrayOf("מצלמה", "גלריה")) { _, which ->
                when (which) {
                    0 -> uploadImageManager.checkAndRequestCameraPermission(requireActivity(), cameraPermissionLauncher)
                    1 -> uploadImageManager.checkAndRequestGalleryPermission(requireActivity(), galleryPermissionLauncher)
                }
            }
            .show()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(mutableListOf()) { user, isActive ->
            // טיפול בשינוי סטטוס משתמש
        }

        binding.profileManagementRVUsers.apply {
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadInitialUsers(username: String) {
        val allUsers = usersRepository.getInitialUsers()
        val filteredUser = allUsers.filter { user ->
            user.userName == username
        }
        userAdapter.updateUsers(filteredUser)
    }
}