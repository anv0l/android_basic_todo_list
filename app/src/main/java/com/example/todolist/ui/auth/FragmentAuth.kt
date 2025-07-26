package com.example.todolist.ui.auth

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.databinding.FragmentAuthBinding
import com.example.todolist.ui.common.UserAvatar
import com.example.todolist.ui.common.helpers.navController
import com.google.android.material.color.MaterialColors
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
                binding.test.text =
                    "state: ${state.toString()}; ${authViewModel.userCredential.value.toString()}"
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
                        val foregroundColor = MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorPrimary,
                            Color.BLACK
                        )
                        val backgroundColor = MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorSurfaceVariant,
                            Color.LTGRAY
                        )
                        val identicon =
                            UserAvatar.generateIdenticon(
                                authViewModel.userCredential.value?.email ?: "",
                                95,
                                backgroundColor = backgroundColor,
                                foregroundColor = foregroundColor
                            )
                        val drawable = UserAvatar.bitmapToDrawable(requireContext(), identicon)
                        binding.txtAuthenticatedUser.setCompoundDrawablesWithIntrinsicBounds(
                            drawable,
                            null,
                            null,
                            null
                        )
                    }

                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.networkState.collect { networkState ->
                when (networkState) {
                    NetworkState.LOADING -> disableAuthButtons()
                    NetworkState.READY -> enableAuthButtons()
                    NetworkState.NOT_AVAILABLE -> enableAuthButtons()
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

        binding.btnRegister.setOnClickListener {
            register()
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

    }

    private fun enableAuthButtons() {
        binding.btnLogin.isEnabled = true
        binding.btnRegister.isEnabled = true
        binding.pbrUserLoading.visibility = View.GONE
    }

    private fun disableAuthButtons() {
        binding.btnLogin.isEnabled = false
        binding.btnRegister.isEnabled = false
        binding.pbrUserLoading.visibility = View.VISIBLE
    }

    private fun login() {
        val email = binding.txtLoginEmail.text.toString()
        val password = binding.txtPassword.text.toString()

        authViewModel.login(email, password)
    }

    private fun register() {
        val email = binding.txtLoginEmail.text.toString()
        val password = binding.txtPassword.text.toString()

        authViewModel.register(email, password)
    }

}