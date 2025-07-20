package com.example.todolist.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.databinding.FragmentAuthBinding
import com.example.todolist.ui.common.helpers.navController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentAuth : Fragment() {
    private lateinit var binding: FragmentAuthBinding
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.authAppBarContainer.setNavigationOnClickListener {
            navController.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.errorDetail.collect { detail ->
                binding.txtErrorDetail.text = detail
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                binding.test.text = "state: ${state.toString()}; ${authViewModel.userCredential.value.toString()}"
                when (state) {
                    AuthState.NOT_LOGGED_IN, AuthState.WRONG_CREDENTIAL -> {
                        binding.authCredContainer.visibility = View.VISIBLE
                        binding.authLogout.visibility = View.GONE
                    }

                    AuthState.AUTHENTICATED -> {
                        binding.authCredContainer.visibility = View.GONE
                        binding.authLogout.visibility = View.VISIBLE
                        binding.txtAuthenticatedUser.text =
                            "Logged in as ${authViewModel.userCredential.value?.email}"
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.mode.collect { mode ->
                when (mode) {
                    AuthMode.LOGIN -> {
                        binding.btnLogin.visibility = View.VISIBLE
                        binding.btnRegister.visibility = View.GONE
                        binding.txtAuthMode.text = "Register"
                    }

                    AuthMode.REGISTER -> {
                        binding.btnLogin.visibility = View.GONE
                        binding.btnRegister.visibility = View.VISIBLE
                        binding.txtAuthMode.text = "Login"
                    }
                }
            }
        }

        binding.txtAuthMode.setOnClickListener {
            authViewModel.toggleMode()
        }

        binding.btnLogin.setOnClickListener {
            login()
        }

    }

    private fun login() {
        val email = binding.txtLoginEmail.text.toString()
        val password = binding.txtPassword.text.toString()

        authViewModel.login(email, password)
    }

    private fun register() {
        val email = binding.txtLoginEmail.text.toString()
        val password = binding.txtPassword.text.toString()
    }
}