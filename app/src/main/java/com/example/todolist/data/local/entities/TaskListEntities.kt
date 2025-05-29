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

    override fun toString(): String {
        return "TaskEntity: id=$id listName=$listName checked=$checked"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaskListEntity
        return id == other.id && listName == other.listName && dateModified == other.dateModified && checked == other.checked && previewItems == other.previewItems
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + listName.hashCode()
        result = 31 * result + dateModified.hashCode()
        result = 31 * result + checked.hashCode()
        result = 31 * result + previewItems.hashCode()
        return result
    }
}

sealed class TaskListItem {
    data class Header(val title: String) : TaskListItem()
    data class Item(val taskItem: TaskItemEntity) : TaskListItem()
}