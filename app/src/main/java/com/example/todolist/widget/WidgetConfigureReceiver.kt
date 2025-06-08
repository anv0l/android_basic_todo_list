package com.example.todolist.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.work.impl.utils.ForceStopRunnable.BroadcastReceiver
import com.example.todolist.R

@SuppressLint("RestrictedApi")
class WidgetConfigureReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val appWidgetId =
            intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val listId = intent?.getLongExtra("list_id", Long.MIN_VALUE) ?: Long.MIN_VALUE

        if (appWidgetId != Int.MIN_VALUE && listId != Long.MIN_VALUE) {
            TaskListWidgetProvider.saveListIdForWidget(context, appWidgetId, listId)

            val appWidgetManager = AppWidgetManager.getInstance(context)
            TaskListWidgetProvider().updateAppWidget(context, appWidgetManager, appWidgetId)
//            appWidgetManager.updateAppWidget(
//                appWidgetId,
//                RemoteViews(context.packageName, R.layout.widget_list)
//            )
        }
    }

    private fun showUnconfiguredWidget(context: Context, widgetId: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_unconfigured)
        val configIntent = Intent(context, WidgetConfigureActivity::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        views.setOnClickPendingIntent(
            R.id.widget_list_main,
            PendingIntent.getActivity(
                context,
                widgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        )

        return views

    }
}