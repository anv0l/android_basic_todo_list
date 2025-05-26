package com.example.todolist.ui.list.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.entities.TaskListEntity
import com.example.todolist.data.local.entities.TaskListItem
import com.example.todolist.data.repository.ListRepository
import com.example.todolist.data.repository.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    private val repository: ListRepository,
    private val prefsRepository: PrefsRepository
) :
    ViewModel() {
    private val _taskLists = MutableStateFlow<List<TaskListEntity>>(emptyList())
    val taskLists = _taskLists.asStateFlow()

    private val _taskListsWithPreview = MutableStateFlow<List<TaskListEntity>>(emptyList())
    val taskListsWithPreview = _taskListsWithPreview.asStateFlow()

    val selectedList = repository.selectedListId
    val checkedLists = repository.checkedLists
    val checkedListsCount = repository.checkedListsCount


    suspend fun getListName(listId: Long): String {
        return repository.getListName(listId).first()
    }

    val maxPreviewItems = prefsRepository.maxPreviewItems

    init {
        viewModelScope.launch {
            repository.getTaskLists().collect {
                _taskLists.value = it
            }
        }

        viewModelScope.launch {
            repository.getTaskListsWithPreview().collect {
                _taskListsWithPreview.value = it
            }
        }
    }


    /*debug*/
    fun debugPrintDatabaseContents() {
        viewModelScope.launch {
            val lists = repository.getTaskLists().first()
            println("task lists in db: ${lists.size}")

            lists.forEach { list ->
                print("List: ${list.listName} (id: ${list.id})")
                val items = repository.observeItemsForList(list.id).first()
                println("  Items: ${items.size}")
                items.forEach { item ->
                    println("   - ${item.itemText}")
                }

            }
        }
    }

    fun toggleList(listId: Long) {
        viewModelScope.launch { repository.toggleList(listId) }
    }

    fun clearListChecks() {
        viewModelScope.launch { repository.clearListChecks() }
    }

    private var toBeDoneTitle = "To be done"
    private var doneTitle = "Done"

    fun updateHeaderTitles(toBeDoneTitle: String, doneTitle: String) {
        this.doneTitle = doneTitle
        this.toBeDoneTitle = toBeDoneTitle
    }

    val selectedListItemsGrouped: StateFlow<List<TaskListItem>> =
        repository.itemsForSelectedList
            .combine(selectedList) { items, _ ->
//                println("combiner received items: ${items.size}")
                val groupedItems = mutableListOf<TaskListItem>()

                val uncheckedItems =
                    items.filter { !it.isChecked }.sortedByDescending { it.dateModified }
                if (uncheckedItems.isNotEmpty()) {
                    groupedItems.add(TaskListItem.Header(toBeDoneTitle))
                    uncheckedItems.forEach { groupedItems.add(TaskListItem.Item(it)) }
                }

                val checkedItems = items.filter { it.isChecked }.sortedBy { it.dateModified }
                if (checkedItems.isNotEmpty()) {
                    groupedItems.add(TaskListItem.Header(doneTitle))
                    checkedItems.forEach { groupedItems.add(TaskListItem.Item(it)) }
                }

                groupedItems
            }.stateIn(
                scope = CoroutineScope(Dispatchers.Default),
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val selectedListName: StateFlow<String> =
        repository.getListName(selectedList.value).combine(selectedList) { list, _ ->
            list
        }.stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.WhileSubscribed(5000),
            ""
        )

//    val selectedListItemsSorted: StateFlow<List<TaskItem>> =
//        taskLists.combine(selectedList) { lists, selectedId ->
//            lists.firstOrNull { it.id == selectedId }?.taskList?.sortedWith(compareBy<TaskItem> { it.checked }.thenBy { item -> if (!item.checked) -item.dateModified.epochSecond else item.dateModified.epochSecond })
//                ?: emptyList()
//        }.stateIn(
//            scope = CoroutineScope(Dispatchers.Default),
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = emptyList()
//        )

//    fun updateAllLists(taskLists: List<TaskList>) {
//        repository.updateAllLists(taskLists)
//    }

    fun selectList(listId: Long) {
        repository.selectList(listId)
    }

    // Lists actions
//    fun addList(newList: TaskList) {
//        repository.addList(newList)
//    }

    fun addEmptyList(listName: String) {
        viewModelScope.launch { repository.addEmptyList(listName) }
    }

//    fun updateList(updatedList: TaskList) {
//        repository.updateList(updatedList)
//    }

    fun renameList(listName: String) {
        viewModelScope.launch { repository.renameList(listName) }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch { repository.deleteList(listId) }
    }

    fun deleteSelectedList() {
        deleteList(selectedList.value)
    }

    fun deleteCheckedLists() {
        viewModelScope.launch { repository.deleteCheckedLists() }
    }


}