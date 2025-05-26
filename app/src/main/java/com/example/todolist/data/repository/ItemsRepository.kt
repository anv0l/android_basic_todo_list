package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskListDao
import javax.inject.Inject

class ItemsRepository @Inject constructor(
    private val taskListDao: TaskListDao
) {

    suspend fun updateItem(itemId: Long, itemName: String) {
        taskListDao.renameItem(itemId, itemName)
    }

    suspend fun deleteItem(itemId: Long) {
        taskListDao.deleteItem(itemId)
    }
}