package com.example.todolist.di

import android.content.Context
import androidx.room.Room
import com.example.todolist.data.local.AppDatabase
import com.example.todolist.data.local.dao.TaskListDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tasklist_database"
        ).build()
    }

    @Provides
    fun provideDao(database: AppDatabase): TaskListDao {
        return database.taskListDao()
    }
}