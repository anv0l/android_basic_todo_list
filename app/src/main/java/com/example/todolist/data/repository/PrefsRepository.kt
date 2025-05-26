package com.example.todolist.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.todolist.ui.common.helpers.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrefsRepository @Inject constructor(private val context: Context) {
    private val LIST_COLUMNS_SINGLE = 1
    private val LIST_COLUMNS_DOUBLE = 2
    private val LIST_COLUMN_DEFAULT = LIST_COLUMNS_DOUBLE
    private val LIST_COLUMNS_KEY = intPreferencesKey("list_columns")
    private val LIST_MAX_PREVIEW_ITEMS_KEY = intPreferencesKey("max_preview_items")

    private val _listColumns = MutableStateFlow(LIST_COLUMN_DEFAULT)
    val listColumns = _listColumns.asStateFlow()

    suspend fun changeListView() {
        context.dataStore.edit { prefs ->
            val currentValue = (prefs[LIST_COLUMNS_KEY] ?: LIST_COLUMN_DEFAULT)
            _listColumns.value =
                if (currentValue == LIST_COLUMNS_SINGLE) LIST_COLUMNS_DOUBLE else LIST_COLUMNS_SINGLE
            prefs[LIST_COLUMNS_KEY] = _listColumns.value
        }
    }

    suspend fun initCols() {
        context.dataStore.data.map { prefs ->
            (prefs[LIST_COLUMNS_KEY] ?: LIST_COLUMN_DEFAULT)
        }.collect { value ->
            _listColumns.value = value
        }
    }

    private val DEFAULT_MAX_PREVIEW = 3

    private val _maxPreviewItems = MutableStateFlow(DEFAULT_MAX_PREVIEW)
    val maxPreviewItems = _maxPreviewItems.asStateFlow()

    suspend fun initMaxPreviewItems() {
        context.dataStore.data.map { prefs ->
            (prefs[LIST_MAX_PREVIEW_ITEMS_KEY] ?: DEFAULT_MAX_PREVIEW)
        }.collect { value ->
            _maxPreviewItems.value = value
        }
    }

    suspend fun updateMaxPreviewItems(newValue: Int) {
        context.dataStore.edit { prefs ->
            _maxPreviewItems.value = newValue
            prefs[LIST_MAX_PREVIEW_ITEMS_KEY] = newValue
        }
    }


}