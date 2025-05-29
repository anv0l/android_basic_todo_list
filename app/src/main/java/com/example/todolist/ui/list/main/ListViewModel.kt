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
    private var _taskListsWithPreview = MutableStateFlow<List<TaskListEntity>>(emptyList())
    val taskListsWithPreview = _taskListsWithPreview

    val selectedListId = repository.selectedListId

    val checkedLists = repository.checkedLists
    val checkedListsCount = repository.checkedListsCount


    suspend fun getListName(listId: Long): String {
        return repository.getListName(listId).first()
    }

    val maxPreviewItems = prefsRepository.maxPreviewItems

    init {
        viewModelScope.launch {
            repository.getTaskListsWithPreview().collect {
                _taskListsWithPreview.value = it
            }
        }
    }

//    fun refreshPreviews() {
//        viewModelScope.launch {
//            repository.getTaskListsWithPreview().collect() { previews ->
//                _taskListsWithPreview.value = previews
//            }
//        }
//    }

    fun toggleList(listId: Long) {
        viewModelScope.launch { repository.toggleList(listId) }
    }

    fun clearListChecks() {
        repository.clearListChecks()
    }

    private var toBeDoneTitle = "To be done"
    private var doneTitle = "Done"

    fun updateHeaderTitles(toBeDoneTitle: String, doneTitle: String) {
        this.doneTitle = doneTitle
        this.toBeDoneTitle = toBeDoneTitle
    }

    val selectedListItemsGrouped: StateFlow<List<TaskListItem>> =
        repository.itemsForSelectedList
            .combine(selectedListId) { items, _ ->
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
        repository.getListName(selectedListId.value).combine(selectedListId) { list, listId ->
            repository.getListName(listId).first()
        }.stateIn(
            CoroutineScope(Dispatchers.Default),
            SharingStarted.Eagerly,
            ""
        )

    fun selectList(listId: Long) {
        repository.selectList(listId)
    }

    fun addEmptyList(listName: String) {
        viewModelScope.launch { repository.addEmptyList(listName) }
    }

    fun renameList(listName: String) {
        viewModelScope.launch { repository.renameList(listName) }
    }

    fun deleteList(listId: Long) {
        viewModelScope.launch { repository.deleteList(listId) }
    }

    fun deleteSelectedList() {
        deleteList(selectedListId.value)
    }

    fun deleteCheckedLists() {
        viewModelScope.launch { repository.deleteCheckedLists() }
    }

}