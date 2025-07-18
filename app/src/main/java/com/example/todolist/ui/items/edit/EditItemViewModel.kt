package com.example.todolist.ui.items.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.repository.ItemsRepository
import com.example.todolist.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditItemViewModel @Inject constructor(
    private val repository: ListRepository,
    private val itemsRepository: ItemsRepository
) :
    ViewModel() {


    private val _isAllItemsChecked = MutableStateFlow(false)
    val isAllItemsChecked = _isAllItemsChecked.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllItemsChecked(repository.selectedListId.value)
                .collect { isAllChecked ->
                    _isAllItemsChecked.value = isAllChecked
                }
        }
    }

    private var _checkedItems = MutableStateFlow<Set<String>>(emptySet())
    val checkedItems = _checkedItems.asStateFlow()

    private var _selectedItemId = MutableStateFlow<String>("")
    val selectedItemId = _selectedItemId.asStateFlow()

    fun selectItem(itemId: String) {
        _selectedItemId.value = itemId
    }

    val itemsForSelectedList: StateFlow<List<TaskItemEntity>> =
        repository.itemsForSelectedList.combine(repository.selectedListId) { items, _ ->
            items
        }.stateIn(
            started = SharingStarted.WhileSubscribed(5000),
            scope = CoroutineScope(Dispatchers.Default),
            initialValue = emptyList()
        )

    fun toggleItem(itemId: String) {
        if (checkedItems.value.contains(itemId))
            _checkedItems.value -= itemId
        else
            _checkedItems.value += itemId
    }

    fun removeAllChecks() {
        _checkedItems.value = emptySet()
    }

    fun deleteCheckedItems() {
        checkedItems.value.forEach { itemId ->
            viewModelScope.launch {
                itemsRepository.deleteItem(itemId)
            }
        }
        removeAllChecks()
    }

    fun renameItem(itemId: String, itemText: String) {
        viewModelScope.launch {
            itemsRepository.updateItem(itemId, itemText)
        }
    }

    // Items actions
    fun addItem(itemText: String) {
        viewModelScope.launch { repository.addItem(itemText) }
    }

    fun deleteItem(itemId: String) {
        viewModelScope.launch { repository.deleteList(itemId) }
    }

    fun toggleItem(listId: String, itemId: String) {
        viewModelScope.launch {
            repository.toggleItem(
                listId = listId,
                itemId = itemId
            )
        }

    }

    fun toggleItemForSelectedList(itemId: String) {
        viewModelScope.launch { repository.toggleItemForSelectedList(itemId) }

    }

    fun toggleAllItems(listId: String) {
        viewModelScope.launch { repository.toggleAllItems(listId) }
    }

    fun toggleAllItemsForSelectedList() {
        toggleAllItems(repository.selectedListId.value)
    }
}