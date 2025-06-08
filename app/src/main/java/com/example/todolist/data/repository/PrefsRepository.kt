package com.example.todolist.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.todolist.data.local.entities.TaskListEntity
import com.example.todolist.ui.common.helpers.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PrefsRepository @Inject constructor(private val context: Context) {
    private val LIST_COLUMNS_KEY = intPreferencesKey("list_columns")
    private val LIST_MAX_PREVIEW_ITEMS_KEY = intPreferencesKey("max_preview_items")
    private val SORT_TYPE_KEY = intPreferencesKey("sort_type")
    private val SORT_ORDER_KEY = intPreferencesKey("sort_order")

    private val _listColumns = MutableStateFlow<Int>(LIST_COLUMN_DEFAULT.index)
    val listColumns = _listColumns.asStateFlow()

    private val _maxPreviewItems = MutableStateFlow(-1)
    val maxPreviewItems = _maxPreviewItems.asStateFlow()

    private val _sortOptions = MutableStateFlow<Pair<SortType, SortOrder>?>(null)
    val sortOptions = _sortOptions.asStateFlow()

    suspend fun changeListView() {
        context.dataStore.edit { prefs ->
            val currentValue = (prefs[LIST_COLUMNS_KEY] ?: LIST_COLUMN_DEFAULT.index)
            _listColumns.value =
                if (currentValue == ListColumns.DOUBLE_COLUMN.index) ListColumns.SINGLE_COLUMN.index else ListColumns.DOUBLE_COLUMN.index
            prefs[LIST_COLUMNS_KEY] = _listColumns.value
        }
    }

    suspend fun initCols() {
        context.dataStore.data.map { prefs ->
            (prefs[LIST_COLUMNS_KEY] ?: LIST_COLUMN_DEFAULT.index)
        }.collect { value ->
            _listColumns.value = value
        }
    }

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

    suspend fun setSortListOptions(newSortType: SortType, newSortOrder: SortOrder) {
        context.dataStore.edit { prefs ->
            _sortOptions.value = Pair(newSortType, newSortOrder)
            prefs[SORT_TYPE_KEY] = newSortType.index
            prefs[SORT_ORDER_KEY] = newSortOrder.index
        }
    }

    suspend fun initSortListOptions() {
        context.dataStore.data.map { prefs ->
            Pair(
                SortType.allTypes[prefs[SORT_TYPE_KEY] ?: 1],
                SortOrder.allOrders[prefs[SORT_ORDER_KEY] ?: 1]
            )
        }.collect { sort ->
            _sortOptions.value = sort
        }
    }


    companion object {
        enum class ListColumns(val index: Int, val listColumns: String) {
            SINGLE_COLUMN(1, "Single"),
            DOUBLE_COLUMN(2, "Double")
        }

        private val LIST_COLUMN_DEFAULT = ListColumns.DOUBLE_COLUMN

        private const val DEFAULT_MAX_PREVIEW = 3

        sealed class SortType(val index: Int, val sortTypeName: String) {
            data object Name : SortType(0, "Sort by name")
            data object TimeModified : SortType(1, "Sort by date modified")

            fun byIndex(index: Int): SortType {
                return when (index) {
                    0 -> Name
                    else -> TimeModified
                }
            }

            companion object {
                val allTypes: List<SortType> by lazy { listOf(Name, TimeModified) }
            }
        }

        sealed class SortOrder(val index: Int, val sortOrder: String) {
            data object OrderAscending : SortOrder(0, "ascending")
            data object OrderDescending : SortOrder(1, "descending")

            fun byIndex(index: Int): SortOrder {
                return when (index) {
                    0 -> OrderAscending
                    else -> OrderDescending
                }
            }

            companion object {
                val allOrders: List<SortOrder> by lazy { listOf(OrderAscending, OrderDescending) }
            }
        }

        fun List<TaskListEntity>.sortedByOption(
            sortOption: Pair<
                    SortType,
                    SortOrder>
        ): List<TaskListEntity> {
            return when (sortOption.first) {
                is SortType.Name -> {
                    when (sortOption.second) {
                        SortOrder.OrderAscending -> sortedBy { it.listName.lowercase() }
                        SortOrder.OrderDescending -> sortedByDescending { it.listName.lowercase() }
                    }
                }

                is SortType.TimeModified -> {
                    when (sortOption.second) {
                        SortOrder.OrderAscending -> sortedBy { it.dateModified }
                        SortOrder.OrderDescending -> sortedByDescending { it.dateModified }
                    }
                }
            }
        }
    }

}