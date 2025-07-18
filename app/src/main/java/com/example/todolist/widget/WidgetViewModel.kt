package com.example.todolist.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.todolist.data.repository.WidgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WidgetViewModel @Inject constructor(private val widgetRepository: WidgetRepository): ViewModel() {
    fun createWidgetForList(context: Context, listId: String) {
        widgetRepository.createWidgetForList(context, listId)
    }
}