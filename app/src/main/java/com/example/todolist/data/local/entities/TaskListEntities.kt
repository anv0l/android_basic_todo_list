package com.example.todolist.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.time.Instant

import java.util.UUID

@Serializable
@Entity(tableName = "task_items")
data class TaskItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "list_id") val listId: String,
    val itemText: String,
    @ColumnInfo(name = "is_checked") val isChecked: Boolean = false,
    @Serializable(with = InstantSerializer::class)
    val dateModified: Instant = Instant.now()
)

@Serializable
@Entity(tableName = "task_lists")
data class TaskListEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val listName: String,
    @Serializable(with = InstantSerializer::class)
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

object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Instant", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.parse(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(value.toString())
    }

}