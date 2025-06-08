package com.example.todolist.ui.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.todolist.data.repository.PrefsRepository
import com.example.todolist.data.repository.PrefsRepository.Companion.SortType
import com.example.todolist.databinding.FragmentBottomSheetSortBinding
import com.example.todolist.ui.common.PrefsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SortListBottomSheet : BottomSheetDialogFragment() {
    private lateinit var adapter: SortListAdapter
    private val prefsViewModel: PrefsViewModel by viewModels()
//    private val sortTypes = listOf<SortType>(SortType.TimeModified, SortType.Name)
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

        binding.lstSortTypes.overScrollMode = View.OVER_SCROLL_NEVER
        binding.lstSortTypes.adapter = adapter
        adapter.sendData(SortType.allTypes)

        viewLifecycleOwner.lifecycleScope.launch {
            prefsViewModel.sortOptions.collect { sortOption ->
                adapter.setCurrentSort(sortOption?.first, sortOption?.second)
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        const val TAG = "SortListBottomSheet"
    }

}