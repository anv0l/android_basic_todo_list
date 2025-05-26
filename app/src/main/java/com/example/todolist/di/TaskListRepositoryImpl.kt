package com.example.todolist.di

import com.example.todolist.data.local.dao.TaskListDao
import com.example.todolist.data.repository.ListRepository
import com.example.todolist.data.repository.PrefsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TaskListRepositoryImpl {

    @Provides
    @Singleton
    fun provideTaskListRepository(
        taskListDao: TaskListDao,
        prefsRepository: PrefsRepository
    ): ListRepository {
        return ListRepository(taskListDao, prefsRepository)
    }

//    @Provides
//    @Singleton
//    fun providesItemsRepository(
//        taskListDao: TaskListDao,
//        commonRepository: CommonRepository,
//    ): ItemsRepository {
//        return ItemsRepository(taskListDao, commonRepository)
//    }
//
//    @Provides
//    @Singleton
//    fun providesCommonRepository(taskListDao: TaskListDao): CommonRepository {
//        return CommonRepository(taskListDao)
//    }
}