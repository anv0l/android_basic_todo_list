package com.example.todolist.di

import android.content.Context
import com.example.todolist.data.repository.EncryptedPrefsRepository
import com.example.todolist.data.repository.PrefsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePrefsRepository(@ApplicationContext context: Context): PrefsRepository {
        return PrefsRepository(context)
    }

    @Provides
    @Singleton
    fun provideEncryptedPrefsRepository(@ApplicationContext context: Context): EncryptedPrefsRepository {
        return EncryptedPrefsRepository(context)
    }
}