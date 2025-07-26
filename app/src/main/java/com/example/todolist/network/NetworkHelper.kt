package com.example.todolist.network

import okio.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import kotlin.Result.Companion.success


class NetworkHelper {
    suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            success(apiCall())
        } catch (e: SocketTimeoutException) {
            Result.failure(Exception("Connection timeout"))
        } catch (e: ConnectException) {
            Result.failure(Exception("Cannot connect to server"))
        } catch (e: IOException) {
            Result.failure(Exception("Network error"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}