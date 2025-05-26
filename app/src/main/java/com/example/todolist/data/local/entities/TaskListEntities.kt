package com.example.todolist.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "task_items")
data class TaskItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "list_id") val listId: Long,
    val itemText: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean = false,
    val dateModified: Instant = Instant.now()
)

@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val listName: String,
    val dateModified: Instant = Instant.now()
) {
    @Ignore
    var previewItems: List<TaskItemEntity> = emptyList()
    var checked: Boolean = false
}

sealed class TaskListItem {
    data class Header(val title: String) : TaskListItem()
    data class Item(val taskItem: TaskItemEntity) : TaskListItem()
}