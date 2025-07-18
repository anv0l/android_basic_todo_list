package com.example.todolist.data.repository

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.todolist.widget.TaskListWidgetProvider
import com.example.todolist.widget.WidgetConfigureReceiver
import javax.inject.Inject

class WidgetRepository @Inject constructor() {
    /*
    * creating widget from application
    * */
    fun createWidgetForList(context: Context, listId: String) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val myProvider = ComponentName(context, TaskListWidgetProvider::class.java)

        if (appWidgetManager.isRequestPinAppWidgetSupported) {
            val successCallback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, WidgetConfigureReceiver::class.java).apply {
                    action = "android.appwidget.action.APPWIDGET_PINNED"
                    putExtra("list_id", listId)
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            val result = appWidgetManager.requestPinAppWidget(myProvider, null, successCallback)
            if (!result) {
                Log.e("Widget", "Failed to request widget pinning")
            }
        } else {
            Toast.makeText(context, "Widget pinning not supported", Toast.LENGTH_SHORT).show()
        }
    }
}