package com.example.todolist.ui.items.active

import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.databinding.VhTaskItemHeaderBinding

class TaskItemHeaderViewHolder(private val binding: VhTaskItemHeaderBinding) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(title: String) {
        binding.txtItemChecked.text = title
    }

}