package com.example.todolist.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.example.todolist.R
import com.example.todolist.TodoApplication
import com.example.todolist.data.local.entities.TaskItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class TaskListRemoteViewFactory(
    private val context: Context,
    private val intent: Intent
) : RemoteViewsFactory {
    private lateinit var items: List<TaskItemEntity>
    private val listId = intent.getLongExtra("list_id", Long.MIN_VALUE)
    private var listName = "ListName placeholder"
    private lateinit var headerViews: RemoteViews

    override fun onCreate() {
        val headerViews = RemoteViews(context.packageName, R.layout.widget_list)
    }

    override fun onDataSetChanged() {
        kotlin.runCatching {
            val dao = (context.applicationContext as TodoApplication).database.taskListDao()
            items = runBlocking {
                dao.getListItemsForWidget(listId).first()
            }.sortedBy {
                if (it.isChecked) it.dateModified.toEpochMilli() else -it.dateModified.toEpochMilli()
            }

            RemoteViews(context.packageName, R.layout.widget_list).setTextViewText(R.id.txt_list_name_widget, listName)
        }.onFailure { e ->
            Log.e("Widget", "Error loading data $e")
            items = emptyList()
        }
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        val views = RemoteViews(context.packageName, R.layout.widget_items)

        setItemProperties(views, item)

        val fillInIntent = Intent().apply {
            putExtra(TaskListWidgetProvider.EXTRA_ITEM_ID, item.id)
        }
        views.setOnClickFillInIntent(R.id.txt_item_text, fillInIntent)

        val dao = (context.applicationContext as TodoApplication).database.taskListDao()
        runBlocking { listName = dao.getListName(listId).first() }

        RemoteViews(context.packageName, R.layout.widget_list).setTextViewText(R.id.txt_list_name_widget, listName)

        return views
    }

    private fun setItemProperties(views: RemoteViews, item: TaskItemEntity) {
        views.setTextViewText(R.id.txt_item_text, item.itemText)

        // It appears that AppWidgets can't properly handle SVG drawables
        // temporal fix by providing PNGs instead
        // TODO: must be reliant an a theme
        val resId = if (item.isChecked) {
            R.drawable.check_box_24dp_000000
        } else {
            R.drawable.check_box_outline_blank_24dp_000000
        }
        views.setTextViewCompoundDrawables(R.id.txt_item_text, resId, 0,0,0)// setImageViewResource(R.id.chk_item_widget, resId)
    }

    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_loading_view)
    }

    override fun getViewTypeCount(): Int {
        return 2 // todo: figure out what this does
    }

    override fun getItemId(position: Int): Long {
        return items[position].id
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}