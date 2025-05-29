package com.example.todolist.ui.list.main

import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskListEntity
import com.example.todolist.databinding.VhTaskListBinding

class TaskListViewHolder(private val binding: VhTaskListBinding) :
    RecyclerView.ViewHolder(binding.root) {
    private val listName = binding.txtListName
    private val listContentPreview = binding.lstContentPreview

    fun bind(
        list: TaskListEntity,
        onClick: (Int) -> Unit,
        onLongClick: (Int) -> Unit
    ) {
        if (list.checked) {
            println("${list.id} is checked")
        }
        else {
            println("${list.id} is UNchecked")
        }
        binding.lstMain.isChecked = list.checked

        listName.text = list.listName
        listContentPreview.removeAllViews()

        list.previewItems.forEach { listItem ->
            TextView(itemView.context).apply {
                text = listItem.itemText
                setPadding(0, 4, 0, 4)
            }.also { listContentPreview.addView(it) }
        }

        itemView.setOnClickListener {
            onClick(adapterPosition)
        }

        itemView.setOnLongClickListener {
            onLongClick(adapterPosition)
            true
        }
    }
}