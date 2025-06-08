package com.example.todolist.ui.options

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.repository.PrefsRepository
import com.example.todolist.data.repository.PrefsRepository.Companion.SortOrder.OrderDescending
import com.example.todolist.data.repository.PrefsRepository.Companion.SortType.TimeModified
import com.example.todolist.databinding.VhSortListOptionsBinding

class SortListAdapter(
    private val onRowClick: (Int) -> Unit
) :
    RecyclerView.Adapter<SortListViewHolder>() {
    private var currentSortType: PrefsRepository.Companion.SortType = TimeModified
    private var currentSortOrder: PrefsRepository.Companion.SortOrder = OrderDescending
    private var sortTypes: List<PrefsRepository.Companion.SortType> = emptyList()

    fun sendData(newSortTypes: List<PrefsRepository.Companion.SortType>) {
        sortTypes = newSortTypes
    }

    fun setCurrentSort(
        newSortType: PrefsRepository.Companion.SortType?,
        newSortOrder: PrefsRepository.Companion.SortOrder?
    ) {
        currentSortOrder = newSortOrder ?: currentSortOrder
        currentSortType = newSortType ?: currentSortType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SortListViewHolder {
        return SortListViewHolder(
            VhSortListOptionsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun getItemCount(): Int {
        return sortTypes.size
    }

    override fun onBindViewHolder(holder: SortListViewHolder, position: Int) {
        val sortType = sortTypes[position]
        holder.bind(sortType, currentSortType, currentSortOrder, onRowClick)
    }
}