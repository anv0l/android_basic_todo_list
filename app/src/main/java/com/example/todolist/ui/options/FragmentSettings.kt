package com.example.todolist.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.databinding.FragmentSettingsBinding
import com.example.todolist.ui.auth.AuthState
import com.example.todolist.ui.auth.AuthViewModel
import com.example.todolist.ui.common.PrefsViewModel
import com.example.todolist.ui.common.helpers.navController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentSettings : Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val prefsViewModel: PrefsViewModel by activityViewModels()
    private val authViewModel: AuthViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        prefsViewModel.initSync()
        authViewModel.initCredentials()
        authViewModel.initAuthState()

        binding.switchSync.setOnClickListener {
            when {
                // can always turn it off
                prefsViewModel.isSyncEnabled.value ->
                    prefsViewModel.toggleSync()

                // if user is authenticated, we can toggle sync anytime
                authViewModel.authState.value == AuthState.AUTHENTICATED ->
                    prefsViewModel.toggleSync()

                // if user is not authenticated, we can't enable sync
                // because it needs valid credentials
                authViewModel.authState.value != AuthState.AUTHENTICATED -> {
                    navController.navigate(FragmentSettingsDirections.actionSettingsToAuth())
                }
            }
        }

        binding.appBarSettings.setNavigationOnClickListener {
            navController.popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.isSyncEnabled.collect { isEnabled ->
                binding.switchSync.isChecked = isEnabled
                binding.txtSyncDescription.text =
                    if (isEnabled) "Sync is enabled" else "Sync is disabled"
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.authState.collect { state ->
                with(binding) {
                    when (state) {
                        AuthState.NOT_LOGGED_IN -> {
                            txtAuthStatus.text = "Not logged in"
                            btnLogout.visibility = View.GONE
                        }

                        AuthState.AUTHENTICATED -> {
                            txtAuthStatus.text =
                                "Logged in as ${authViewModel.userCredential.value?.email}"
                            btnLogout.visibility = View.VISIBLE
                        }

                        AuthState.WRONG_CREDENTIAL -> {
                            txtAuthStatus.text = "Invalid credentials"
                            btnLogout.visibility = View.GONE
                        }
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
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }
}