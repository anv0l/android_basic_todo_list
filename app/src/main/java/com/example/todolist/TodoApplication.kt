package com.example.todolist

import android.app.Application
import androidx.room.Room
import com.example.todolist.data.local.AppDatabase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TodoApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(applicationContext, AppDatabase::class.java, "tasklist_database")
            .build()
    }
}