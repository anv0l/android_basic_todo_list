package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskListDao
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.local.entities.TaskListEntity
import com.example.todolist.data.repository.PrefsRepository.Companion.sortedByOption
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import java.time.Instant
import javax.inject.Inject


class ListRepository @Inject constructor(
    private val taskListDao: TaskListDao,
    private val prefsRepository: PrefsRepository
) {

    private val _selectedListId = MutableStateFlow<String>("")
    val selectedListId: StateFlow<String> = _selectedListId.asStateFlow()

    private fun getSelectedListId(): String {
        return _selectedListId.value
    }

    fun selectList(listId: String) {
        _selectedListId.value = listId
    }

    /*
    * Lists
    * */
    private var _checkedLists = MutableStateFlow<Set<String>>(emptySet())
    val checkedLists = _checkedLists.asStateFlow()

    /*
   * Lists
   * */
    fun getTaskListsWithPreview(): Flow<List<TaskListEntity>> {
        return taskListDao.getAllLists().combine(checkedLists) { lists, checkedIds ->
            lists.map { list ->
                val maxPreviewCount = prefsRepository.maxPreviewItems.value
                if (maxPreviewCount == -1) {
                    prefsRepository.initCols()
                }
                val previews =
                    taskListDao.getTaskItemsPreview(list.id, prefsRepository.maxPreviewItems.value)
                        .first()
                list.copy().apply {
                    previewItems = previews
                    checked = checkedIds.contains(list.id)
                }
            }
        }.combine(prefsRepository.sortOptions) { lists, sortOptions ->
            sortOptions?.let { (_, _) ->
                lists.sortedByOption(sortOptions)
            } ?: lists
        }
    }

    /*
    * common
    * */
    fun getListName(listId: String) = taskListDao.getListName(listId)

    /*
    *  items
    * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsForSelectedList: Flow<List<TaskItemEntity>> =
        selectedListId.flatMapLatest { listId -> taskListDao.observeItemsForList(listId) }

    /*
    * lists
    * */
    suspend fun addList(newList: TaskListEntity): String {
        taskListDao.insertList(newList)
        return newList.id
    }

    /*
    * lists
    * */
    suspend fun addEmptyList(listName: String) {
        val listId =
            addList(
                TaskListEntity(
                    listName = listName,
                    dateModified = Instant.now()
                )
            )
        selectList(listId)
    }

    /*
    * common (called from lists and items
    * */
    suspend fun renameList(listName: String) {
        taskListDao.renameList(getSelectedListId(), listName)
    }

    /*
    * lists
    * */
    fun toggleList(listId: String) {
        _checkedLists.value = if (checkedLists.value.contains(listId)) {
            checkedLists.value - listId
        } else {
            checkedLists.value + listId
        }
    }

    /*
    * lists
    * */
    fun clearListChecks() {
        _checkedLists.value = emptySet()
    }

    /*
    * common, called from lists and items
    * */
    suspend fun deleteList(listId: String) {
        val listEntity = TaskListEntity(
            id = listId,
            listName = "",
            dateModified = Instant.now()
        )
        taskListDao.deleteList(listEntity)
    }

    /*
    * lists
    * */
    suspend fun deleteCheckedLists() {
        checkedLists.value.forEach { listId ->
            deleteList(listId)
        }
        clearListChecks()
    }


    suspend fun addItem(itemText: String) {
        val currentListId = getSelectedListId()
        val taskList = TaskItemEntity(
            listId = currentListId,
            itemText = itemText,
            isChecked = false,
            dateModified = Instant.now()
        )
        taskListDao.insertItem(taskList)
    }

    suspend fun toggleItem(listId: String, itemId: String) {
        taskListDao.toggleItem(listId, itemId)
    }

    suspend fun toggleItemForSelectedList(itemId: String) {
        toggleItem(getSelectedListId(), itemId)
    }

    suspend fun toggleAllItems(listId: String) {
        taskListDao.toggleAllItems(listId)
    }

    fun observeAllItemsChecked(listId: String): Flow<Boolean> {
        return taskListDao.observeAllItemsChecked(listId)
            .distinctUntilChanged()
    }

}