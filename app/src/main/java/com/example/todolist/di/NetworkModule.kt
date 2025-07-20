package com.example.todolist.di

import com.example.todolist.network.ApiAuth
import com.example.todolist.network.AuthInterceptor
import com.example.todolist.network.AuthManager
import com.example.todolist.network.AuthService
import com.example.todolist.network.buildRetrofit
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

@Module
@InstallIn(ViewModelComponent::class)
abstract class MainModule {
    @Binds
    abstract fun authService(impl: AuthService.Impl): AuthService

    @Binds
    abstract fun authManager(impl: AuthManager.Impl): AuthManager
}

@Module
@InstallIn(ViewModelComponent::class)
class MainModuleProvider {
    @Provides
    fun okHttp(authInterceptor: AuthInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .callTimeout(30L, TimeUnit.SECONDS)
//            .addInterceptor(authInterceptor)
            .addInterceptor(HttpLoggingInterceptor().apply {
                setLevel(HttpLoggingInterceptor.Level.BASIC)
            })
            .build()
    }

    @Provides
    fun retrofit(okhttp: OkHttpClient): Retrofit {
        return buildRetrofit(okhttp)
    }

    @Provides
    fun apiAuth(retrofit: Retrofit): ApiAuth {
        return retrofit.create(ApiAuth::class.java)
    }
}

@Module
@InstallIn(SingletonComponent::class)
class AuthInterceptorProvider {
    @Provides
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()
}