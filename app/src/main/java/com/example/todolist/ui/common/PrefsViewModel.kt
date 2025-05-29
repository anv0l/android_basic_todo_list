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

    fun initCols() {
        viewModelScope.launch {
            prefsRepository.initCols()
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
}