package com.example.todolist.data.repository

import com.example.todolist.data.local.dao.TaskListDao
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.local.entities.TaskListEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.Instant
import javax.inject.Inject


class ListRepository @Inject constructor(
    private val taskListDao: TaskListDao,
    private val prefsRepository: PrefsRepository
) {

    private val _selectedListId = MutableStateFlow<Long>(-1)
    val selectedListId: StateFlow<Long> = _selectedListId.asStateFlow()

    private fun getSelectedListId(): Long {
        return _selectedListId.value
    }

    fun selectList(listId: Long) {
        _selectedListId.value = listId
    }

    /*
    * Lists
    * */
    private var _checkedLists = MutableStateFlow<Set<Long>>(emptySet())
    val checkedLists = _checkedLists.asStateFlow()

    /*
   * Lists
   * */

    /*
   * Lists
   * */
    fun getTaskListsWithPreview(): Flow<List<TaskListEntity>> {
        return taskListDao.getAllListsSorted().combine(checkedLists) { lists, checkedIds ->
            lists.map { list ->
                val previews =
                    taskListDao.getTaskItemsPreview(list.id, prefsRepository.maxPreviewItems.value)
                        .first()
                list.copy().apply {
                    previewItems = previews
                    checked = checkedIds.contains(list.id)
                }
            }
        }
    }

    /*
    * Lists
    * */
    val checkedListsCount = checkedLists.combine(checkedLists) { lists, _ ->
        lists.size
    }.stateIn(
        scope = CoroutineScope(Dispatchers.Default),
        initialValue = 0,
        started = SharingStarted.WhileSubscribed(5000)
    )

    /*
    * common
    * */
    fun getListName(listId: Long) = taskListDao.getListName(listId)

    /*
    *  items
    * */
    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsForSelectedList: Flow<List<TaskItemEntity>> =
        selectedListId.flatMapLatest { listId -> taskListDao.observeItemsForList(listId) }

    /*
    * items
    * */
//    fun observeItemsForList(listId: Long): Flow<List<TaskItemEntity>> =
//        taskListDao.observeItemsForList(listId)

    /*
    * lists
    * */
    suspend fun addList(newList: TaskListEntity): Long {
        return taskListDao.insertList(newList)
    }


    /*
    * lists
    * */
    suspend fun addEmptyList(listName: String) {
        val listId =
            addList(
                TaskListEntity(
                    id = 0,
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
    fun toggleList(listId: Long) {
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
    suspend fun deleteList(listId: Long) {
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
            id = 0,
            listId = currentListId,
            itemText = itemText,
            isChecked = false,
            dateModified = Instant.now()
        )
        taskListDao.insertItem(taskList)
    }

    suspend fun toggleItem(listId: Long, itemId: Long) {
        taskListDao.toggleItem(listId, itemId)
    }

    suspend fun toggleItemForSelectedList(itemId: Long) {
        toggleItem(getSelectedListId(), itemId)
    }

    suspend fun toggleAllItems(listId: Long) {
        taskListDao.toggleAllItems(listId)
    }

    fun observeAllItemsChecked(listId: Long): Flow<Boolean> {
        return taskListDao.observeAllItemsChecked(listId)
            .distinctUntilChanged()
    }


}