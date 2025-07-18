package com.example.todolist.ui.items.edit

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.databinding.VhTaskItemEditBinding

class EditTaskItemAdapter(
    private val onClick: (Int) -> Unit,
    private val onLongClick: (Int) -> Unit,
    private var checkedItems: Set<String> = emptySet()
) :
    ListAdapter<TaskItemEntity, RecyclerView.ViewHolder>(EditTaskItemDiff) {


    fun updateCheckedItems(newCheckedItem: Set<String>) {
        checkedItems = newCheckedItem
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as EditTaskItemViewHolder).bind(item, onClick, onLongClick, checkedItems.contains(item.id))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return EditTaskItemViewHolder(
            VhTaskItemEditBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }
}

private object EditTaskItemDiff : DiffUtil.ItemCallback<TaskItemEntity>() {
    override fun areItemsTheSame(oldItem: TaskItemEntity, newItem: TaskItemEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskItemEntity, newItem: TaskItemEntity): Boolean {
        return oldItem == newItem
    }
}