package com.example.todolist.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.todolist.data.local.converters.InstantConverter
import com.example.todolist.data.local.dao.TaskListDao
import com.example.todolist.data.local.entities.TaskItemEntity
import com.example.todolist.data.local.entities.TaskListEntity

@Database(
    entities = [TaskListEntity::class, TaskItemEntity::class],
    version = 1
)
@TypeConverters(InstantConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskListDao(): TaskListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "tasklist_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}