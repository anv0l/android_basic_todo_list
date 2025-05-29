package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskListDao
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.local.entities.TaskListEntity
import java.time.Instant
import javax.inject.Inject


class ListImportRepository @Inject constructor(
    private val taskListDao: TaskListDao,
    private val repository: ListRepository
) {
    suspend fun importList(listName: String, listItems: List<String>) {
        val listId = repository.addList(
            TaskListEntity(
                id = 0,
                listName = listName,
                dateModified = Instant.now()
            )
        )
        repository.selectList(listId)
        listItems.forEach { item ->
            val itemId = taskListDao.insertItem(
                TaskItemEntity(
                    id = 0,
                    itemText = item,
                    dateModified = Instant.now(),
                    listId = listId
                )

            )
        }
    }
}