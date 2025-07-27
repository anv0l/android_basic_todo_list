package com.example.todolist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
import android.widget.RemoteViews.RemoteCollectionItems
import com.example.todolist.R
import com.example.todolist.TodoApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TaskListWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { widgetId ->
            val listId = getListIdForWidget(context, widgetId)
            if (listId.isEmpty()) {
                val views = RemoteViews(context.packageName, R.layout.widget_unconfigured)
                appWidgetManager.updateAppWidget(widgetId, views)
            } else {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray?) {
        appWidgetIds?.forEach { widgetId ->
            deleteWidgetPrefs(context, widgetId)
        }
    }

    fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
        val listId = getListIdForWidget(context, appWidgetId)
        if (listId.isEmpty()) return

        val views = RemoteViews(context.packageName, R.layout.widget_list)
        val listName = runBlocking { getListNameForWidget(context, listId) }
        views.setTextViewText(R.id.txt_list_name_widget, listName)

        val items = createRemoteCollectionItems(context, listId, appWidgetId)
        views.setRemoteAdapter(R.id.lst_list_container, items)

        val clickIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_ITEM
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("todo://widget/id/#$appWidgetId")
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.lst_list_container, pendingIntent)

        val clickSyncIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_ITEMS
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse("todo://widget/id/#$appWidgetId")
        }
        val pendingSyncIntent = PendingIntent.getBroadcast(
            context,
            appWidgetId,
            clickSyncIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setOnClickPendingIntent(R.id.txt_list_name_widget, pendingSyncIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)

    }

    private fun createRemoteCollectionItems(
        context: Context,
        listId: String,
        appWidgetId: Int
    ): RemoteCollectionItems {
        val dao = (context.applicationContext as TodoApplication).database.taskListDao()
        val items = runBlocking {
            dao.getListItemsForWidget(listId).first().sortedBy {
                if (it.isChecked) it.dateModified.toEpochMilli() else -it.dateModified.toEpochMilli()
            }
        }

        return RemoteCollectionItems.Builder().run {
            setHasStableIds(true)

            items.forEach { item ->
                val itemView = RemoteViews(context.packageName, R.layout.widget_items).apply {
                    setTextViewText(R.id.txt_item_text, item.itemText)
                    setCompoundButtonChecked(R.id.chk_widget_item, item.isChecked)

                    val fillInIntent = Intent().apply {
                        putExtra(EXTRA_ITEM_ID, item.id)
                        putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    }
                    setOnClickFillInIntent(R.id.chk_widget_item, fillInIntent)
                    setOnClickFillInIntent(R.id.txt_item_text, fillInIntent)
                }
                addItem(item.id.hashCode().toLong(), itemView)
            }
            build()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("WidgetDebug", "All extras: ${intent.extras?.keySet()?.joinToString()}")

        if (intent.action == ACTION_TOGGLE_ITEM) {
            val itemId = intent.getStringExtra(EXTRA_ITEM_ID) ?: ""

            if (itemId != "") {
                val widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )

                CoroutineScope(Dispatchers.IO).launch {
                    val dao =
                        (context.applicationContext as TodoApplication).database.taskListDao()

                    val currentItem = dao.getItemForWidget(itemId).first()
                    dao.toggleItem(currentItem.id)

                    // TODO: replace with something that is not deprecated
                    AppWidgetManager.getInstance(context)
                        .notifyAppWidgetViewDataChanged(widgetId, R.id.lst_list_container)

                    if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                        val manager = AppWidgetManager.getInstance(context)
                        updateAppWidget(context, manager, widgetId)
                    }
                }
            }
        } else if (intent.action == ACTION_REFRESH_ITEMS) {
            val widgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
            if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                AppWidgetManager.getInstance(context)
                    .notifyAppWidgetViewDataChanged(widgetId, R.id.lst_list_container) // TODO: deprecation

                val manager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, manager, widgetId)
            }

        }
    }

    companion object {
        const val ACTION_TOGGLE_ITEM = "CHECK_ITEM"
        const val ACTION_REFRESH_ITEMS = "REFRESH_ITEMS"
        const val EXTRA_ITEM_ID = "ITEM_ID"

        fun saveListIdForWidget(context: Context, appWidgetId: Int, listId: String) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("widget_$appWidgetId", listId).apply()
        }

        fun getListIdForWidget(context: Context, appWidgetId: Int): String {
            val prefs =
                context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            return prefs.getString("widget_$appWidgetId", "") ?: ""
        }

        fun deleteWidgetPrefs(context: Context, appWidgetId: Int) {
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().remove("widget_$appWidgetId").apply()
        }
    }

    private fun getListNameForWidget(context: Context, listId: String): String {
        return runBlocking {
            try {
                val dao = (context.applicationContext as TodoApplication).database.taskListDao()
                dao.getListName(listId).first()
            } catch (e: Exception) {
                Log.e("Widget", "Couldn't get list name: $e")
                "Error getting name"
            }
        }
    }
}
