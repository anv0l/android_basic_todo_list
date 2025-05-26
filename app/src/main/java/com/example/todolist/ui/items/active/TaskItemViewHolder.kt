package com.example.todolist.ui.items.active

import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskListItem
import com.example.todolist.databinding.VhTaskItemBinding

class TaskItemViewHolder(binding: VhTaskItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val checkBox = binding.chkItem
    private val text = binding.txtItem

    fun bind(item: TaskListItem.Item, onClick: (Int) -> Unit) {
        checkBox.isChecked = item.taskItem.isChecked
        text.text = item.taskItem.itemText

        itemView.setOnClickListener {
            onClick(adapterPosition)
        }
        checkBox.setOnClickListener {
            onClick(adapterPosition)
        }
    }
}