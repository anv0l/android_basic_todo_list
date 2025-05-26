package com.example.todolist.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.todolist.R

class TaskListWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        appWidgetIds?.forEach { appWidgetId ->
            updateAppWidget(context!!, appWidgetManager!!, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_list)

//        val checkIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
//            action = "CHECK_ITEM"
//            putExtras(appWidgetManager., appWidgetId)
//
//            val pendingIntent = PendingIntent.getBroadcast(
//                context,
//                0,
//                checkIntent,
//                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//            )
//
//            appWidgetManager.updateAppWidget(appWidgetId, views)
//        }
    }
}