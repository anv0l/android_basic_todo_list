package com.example.todolist.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.RemoteViews
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
        val listId = getListIdForWidget(context)
        val listName = getListNameForWidget(context, listId)

        val views = RemoteViews(context.packageName, R.layout.widget_list)
        views.setTextViewText(R.id.txt_list_name_widget, listName)

        val templateIntent =
            makeTemplateIntent(context, ACTION_TOGGLE_ITEM, appWidgetIds, "list_id" to listId)
        val pendingIntent = makePendingIntent(context, templateIntent)
        views.setPendingIntentTemplate(R.id.lst_list_container, pendingIntent)

        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun makeTemplateIntent(
        context: Context,
        actionName: String,
        appWidgetIds: IntArray,
        vararg extras: Pair<String, Any>
    ): Intent {
        return Intent(context, TaskListWidgetProvider::class.java).apply {
            action = actionName
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds)
            data = Uri.parse("todo://widget/id/#$appWidgetIds")

            extras.forEach { (key, value) ->
                when (value) {
                    is Int -> putExtra(key, value)
                    is String -> putExtra(key, value)
                    is Boolean -> putExtra(key, value)
                    is Long -> putExtra(key, value)
                    else -> throw IllegalArgumentException("Unsupported extra type: ${value.javaClass}")
                }
            }
        }
    }

    private fun makePendingIntent(context: Context, templateIntent: Intent): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            templateIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_list)

        val listId = getListIdForWidget(context)
        val listName = runBlocking {
            getListNameForWidget(context, listId)
        }
        views.setTextViewText(R.id.txt_list_name_widget, listName)

        val intent = Intent(context, TaskListWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra("list_id", listId) // todo: need to get from settings I guess?
        }
        views.setRemoteAdapter(R.id.lst_list_container, intent) // todo: get rid of deprecation

        val clickIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_ITEM
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
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
        views.setOnClickPendingIntent(R.id.btn_sync_list, pendingSyncIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }


    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        Log.d("WidgetDebug", "All extras: ${intent.extras?.keySet()?.joinToString()}")

        if (intent.action == ACTION_TOGGLE_ITEM) {
            val itemId = intent.getLongExtra(EXTRA_ITEM_ID, -1L)

            if (itemId != -1L) {
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
                    .notifyAppWidgetViewDataChanged(widgetId, R.id.lst_list_container)

                val manager = AppWidgetManager.getInstance(context)
                updateAppWidget(context, manager, widgetId)
            }

        }
    }

    companion object {
        const val ACTION_TOGGLE_ITEM = "CHECK_ITEM"
        const val ACTION_REFRESH_ITEMS = "REFRESH_ITEMS"
        const val EXTRA_ITEM_ID = "ITEM_ID"

        // todo: how can I get a listId for this exact widget? What if there are more than 1 widget?
        private fun getListIdForWidget(context: Context): Long {
            return -1L
        }

        private fun getListNameForWidget(context: Context, listId: Long): String {
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
}