package com.example.todolist.ui.common.model

import androidx.lifecycle.ViewModel
import com.example.todolist.data.repository.ListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class TaskListViewModel @Inject constructor(private val repository: ListRepository) :
    ViewModel() {

}