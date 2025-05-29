package com.example.todolist.ui.list.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.todolist.data.local.entities.TaskListEntity
import com.example.todolist.databinding.VhTaskListBinding

class TaskListAdapter(
    private var onClick: (Int) -> Unit,
    private var onLongClick: (Int) -> Unit,
    private var selectedLists: Set<Long> = emptySet()
) :
    ListAdapter<TaskListEntity, RecyclerView.ViewHolder>(TaskListDiff) {
//
//    fun updateSelectedIds(newSelectedSet: Set<Long>) {
//        selectedLists = newSelectedSet
//        notifyDataSetChanged() // todo: change to only refresh newly checked
//    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TaskListViewHolder(
            VhTaskListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as TaskListViewHolder).bind(
            list = item,
            onClick = onClick,
            onLongClick = onLongClick
        )
    }
}

private object TaskListDiff : DiffUtil.ItemCallback<TaskListEntity>() {
    override fun areItemsTheSame(oldItem: TaskListEntity, newItem: TaskListEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TaskListEntity, newItem: TaskListEntity): Boolean {
        return oldItem == newItem && oldItem.checked == newItem.checked
    }
}
