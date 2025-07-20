package com.example.todolist.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor :
    Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestWithToken = request.newBuilder()
            .header("Content-Type", "application/json")
            .build()

        return chain.proceed(requestWithToken)
    }
}