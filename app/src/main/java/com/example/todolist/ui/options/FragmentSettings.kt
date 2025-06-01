package com.example.todolist.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.R
import com.example.todolist.databinding.FragmentSettingsBinding
import com.example.todolist.ui.common.PrefsViewModel
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FragmentSettings: Fragment() {
    private lateinit var binding: FragmentSettingsBinding
    private val prefsViewModel: PrefsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPreviewSettings()

    }

    private fun setupPreviewSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.maxPreviewItems.collect { value ->
                binding.txtPreviewCountDescription.text =
                    getString(R.string.maximum_preview_items_1_s, value)
                binding.sliderPreviewCount.value = value.toFloat()
            }
        }

        binding.sliderPreviewCount.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {

            }

            override fun onStopTrackingTouch(slider: Slider) {
                prefsViewModel.updateMaxPreviewItems(slider.value.toInt())
            }
        })

        binding.sliderPreviewCount.addOnChangeListener { _, value, _ ->
            binding.txtPreviewCountDescription.text =
                getString(R.string.maximum_preview_items_1_s, value.toInt())
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