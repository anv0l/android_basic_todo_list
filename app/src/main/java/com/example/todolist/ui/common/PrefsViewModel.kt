package com.example.todolist.ui.common

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.PrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrefsViewModel @Inject constructor(private val prefsRepository: PrefsRepository) :
    ViewModel() {
    val listColumns = prefsRepository.listColumns
    val maxPreviewItems = prefsRepository.maxPreviewItems
    val sortOptions = prefsRepository.sortOptions
    val isSyncEnabled = prefsRepository.isSyncEnabled

    fun initCols() {
        viewModelScope.launch {
            prefsRepository.initCols()
        }
    }

    fun initSync() {
        viewModelScope.launch {
            prefsRepository.initSync()
        }
    }

    fun changeListView() {
        viewModelScope.launch {
            prefsRepository.changeListView()
        }
    }

    fun initMaxPreviewItems() {
        viewModelScope.launch {
            prefsRepository.initMaxPreviewItems()
        }
    }

    fun updateMaxPreviewItems(newValue: Int) {
        viewModelScope.launch {
            prefsRepository.updateMaxPreviewItems(newValue)
        }
    }

    fun initSortListOptions() {
        viewModelScope.launch { prefsRepository.initSortListOptions() }
    }

    fun setSortingOptions(
        newSortType: PrefsRepository.Companion.SortType,
        newSortOrder: PrefsRepository.Companion.SortOrder
    ) {
        viewModelScope.launch {
            prefsRepository.setSortListOptions(newSortType, newSortOrder)
        }

    }

    fun toggleSync() {
        viewModelScope.launch {
            prefsRepository.toggleSync()
        }
    }

}