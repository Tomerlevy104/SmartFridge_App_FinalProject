package com.example.smartfridge_app_finalproject.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.smartfridge_app_finalproject.R
import com.example.smartfridge_app_finalproject.adapters.UserAdapter
import com.example.smartfridge_app_finalproject.data.model.User
import com.example.smartfridge_app_finalproject.data.repository.UsersRepository
import com.example.smartfridge_app_finalproject.databinding.FragmentManageProfileBinding


class ManageProfileFragment : Fragment() {

    private lateinit var binding: FragmentManageProfileBinding
    private lateinit var userAdapter: UserAdapter
    private var usersRepository= UsersRepository()

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
        //Call with specific username
        loadInitialUsers("benny_l")


    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(mutableListOf()) { user, isActive ->
            // Handle user status change
            //user.isActive = isActive
        }

        binding.profileManagementRVUsers.apply {
            adapter = userAdapter
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun loadInitialUsers(username: String) {
        //Get all users from repository
        val allUsers = usersRepository.getInitialUsers()

        // Filter user based on username
        val filteredUser = allUsers.filter { user ->
            user.userName == username
        }

        userAdapter.updateUsers(filteredUser)
    }

}