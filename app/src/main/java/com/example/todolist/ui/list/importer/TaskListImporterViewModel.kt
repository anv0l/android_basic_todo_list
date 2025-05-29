package com.example.todolist.ui.list.importer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist.data.repository.ListImportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskListImporterViewModel @Inject constructor(private val listImportRepository: ListImportRepository) :
    ViewModel() {
    fun importList(listName: String, listItems: List<String>) {
        viewModelScope.launch {
            listImportRepository.importList(listName, listItems)
        }
    }
}