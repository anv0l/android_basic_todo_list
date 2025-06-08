package com.example.todolist.ui.options

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.R
import com.example.todolist.data.repository.PrefsRepository
import com.example.todolist.databinding.VhSortListOptionsBinding

class SortListViewHolder(binding: VhSortListOptionsBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private val txtSortType = binding.txtSortType
    private val imgSortOrder = binding.imgSortOrder

    fun bind(
        sortType: PrefsRepository.Companion.SortType,
        currentSortType: PrefsRepository.Companion.SortType? = null,
        currentSortOrder: PrefsRepository.Companion.SortOrder? = null,
        onRowClick: (Int) -> Unit
    ) {
        txtSortType.text = sortType.sortTypeName
        txtSortType.setOnClickListener {
            onRowClick(adapterPosition)
        }
        imgSortOrder.setOnClickListener {
            onRowClick(adapterPosition)
        }
        if (currentSortOrder == null || currentSortType != sortType) {
            imgSortOrder.visibility = View.GONE
            txtSortType.paintFlags = 0
        } else {
            txtSortType.paintFlags = android.graphics.Paint.UNDERLINE_TEXT_FLAG
            imgSortOrder.visibility = View.VISIBLE
            val resId =
                if (currentSortOrder.byIndex(currentSortOrder.index) == PrefsRepository.Companion.SortOrder.OrderDescending) {
                    R.drawable.south_24dp
                } else {
                    R.drawable.north_24dp
                }
            imgSortOrder.setImageResource(resId)
        }

    }
}