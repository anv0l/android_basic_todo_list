package com.example.todolist.ui.items.edit

import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.databinding.VhTaskItemEditBinding

class EditTaskItemViewHolder(binding: VhTaskItemEditBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val checkBox = binding.chkItem
    private val text = binding.txtItem

    fun bind(
        item: TaskItemEntity,
        onClick: (Int) -> Unit,
        onLongClick: (Int) -> Unit,
        itemChecked: Boolean = false
    ) {
        text.text = item.itemText
        checkBox.isChecked = itemChecked

        itemView.setOnClickListener {
            onClick(adapterPosition)
        }

        itemView.setOnLongClickListener {
            onLongClick(adapterPosition)
            true
        }

        checkBox.setOnClickListener {
            onClick(adapterPosition)
        }
    }
}