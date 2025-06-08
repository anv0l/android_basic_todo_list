package com.example.todolist.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.R
import com.example.todolist.data.repository.PrefsRepository
import com.example.todolist.data.repository.PrefsRepository.Companion.SortType
import com.example.todolist.databinding.FragmentBottomSheetSortBinding
import com.example.todolist.ui.common.PrefsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.slider.Slider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SortListBottomSheet : BottomSheetDialogFragment() {
    private lateinit var adapter: SortListAdapter
    private val prefsViewModel: PrefsViewModel by viewModels()
    private lateinit var binding: FragmentBottomSheetSortBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBottomSheetSortBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupPreviewSettings()

        adapter = SortListAdapter() { pos ->
            val sortType = SortType.allTypes[pos]
            println(SortType.allTypes.joinToString(","))
            if (prefsViewModel.sortOptions.value?.first?.index != sortType.index) {
                prefsViewModel.setSortingOptions(
                    sortType,
                    PrefsRepository.Companion.SortOrder.OrderDescending /*always reset order when changing type*/
                )
            } else {
                prefsViewModel.setSortingOptions(
                    sortType,
                    if (prefsViewModel.sortOptions.value?.second == PrefsRepository.Companion.SortOrder.OrderAscending)
                        PrefsRepository.Companion.SortOrder.OrderDescending
                    else
                        PrefsRepository.Companion.SortOrder.OrderAscending
                )
            }
        }

        binding.txtToggleGridView.setOnClickListener {
            prefsViewModel.changeListView()
        }
        binding.btnToggleGridView.setOnClickListener {
            prefsViewModel.changeListView()
        }

        binding.lstSortTypes.overScrollMode = View.OVER_SCROLL_NEVER
        binding.lstSortTypes.adapter = adapter
        adapter.sendData(SortType.allTypes)

        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.sortOptions.collect { sortOption ->
                adapter.setCurrentSort(sortOption?.first, sortOption?.second)
                adapter.notifyDataSetChanged()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.listColumns.collect { value ->
                when (value) {
                    PrefsRepository.Companion.ListColumns.DOUBLE_COLUMN.index -> binding.btnToggleGridView.setImageResource(
                        R.drawable.view_agenda_24dp
                    )

                    PrefsRepository.Companion.ListColumns.SINGLE_COLUMN.index -> binding.btnToggleGridView.setImageResource(
                        R.drawable.view_comfy_alt_24dp
                    )
                }

            }
        }
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

    companion object {
        const val TAG = "SortListBottomSheet"
    }

}