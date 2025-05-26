package com.example.todolist.ui.items.active

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskListItem
import com.example.todolist.databinding.VhTaskItemBinding
import com.example.todolist.databinding.VhTaskItemHeaderBinding

class TaskItemAdapter(
    private val onClick: (Int) -> Unit
) :
    ListAdapter<TaskListItem, RecyclerView.ViewHolder>(TaskItemDiff) {

    fun onItemMove(fromPosition: Int, toPosition: Int) {

    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TaskListItem.Header -> HEADER
            is TaskListItem.Item -> ITEM
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TaskItemViewHolder -> {
                val item = getItem(position) as TaskListItem.Item
                (holder as TaskItemViewHolder).bind(item, onClick)
            }

            is TaskItemHeaderViewHolder -> {
                val item = (getItem(position) as TaskListItem.Header)
                (holder as TaskItemHeaderViewHolder).bind(item.title)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM -> TaskItemViewHolder(
                VhTaskItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            HEADER -> TaskItemHeaderViewHolder(
                VhTaskItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    companion object {
        const val HEADER = 0
        const val ITEM = 1
    }
}

private object TaskItemDiff : DiffUtil.ItemCallback<TaskListItem>() {
    override fun areItemsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return when {
            (oldItem is TaskListItem.Item && newItem is TaskListItem.Item) ->
                oldItem.taskItem.id == newItem.taskItem.id

            (oldItem is TaskListItem.Header && newItem is TaskListItem.Header) ->
                oldItem.title == newItem.title

            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: TaskListItem, newItem: TaskListItem): Boolean {
        return oldItem == newItem
    }

}