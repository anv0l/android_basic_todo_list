package com.example.todolist.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TaskListWidgetService: RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskListRemoteViewFactory(this.applicationContext, intent)
    }

}