package com.example.community_app.core.data.http_client

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException
import okio.IOException

suspend inline fun <reified T> safeCall(
  execute: () -> HttpResponse
): Result<T, DataError.Remote> {
  val response = try {
    execute()
  } catch (e: SocketTimeoutException) {
    e.printStackTrace()
    return Result.Error(DataError.Remote.REQUEST_TIMEOUT)
  } catch (e: UnresolvedAddressException) {
    e.printStackTrace()
    return Result.Error(DataError.Remote.NO_INTERNET)
  } catch (e: IOException) {
    e.printStackTrace()
    return Result.Error(DataError.Remote.NO_INTERNET)
  } catch (e: Exception) {
    currentCoroutineContext().ensureActive()
    e.printStackTrace()
    return Result.Error(DataError.Remote.UNKNOWN)
  }

  return try {
    responseToResult(response)
  } catch (e: SerializationException) {
    e.printStackTrace()
    Result.Error(DataError.Remote.SERIALIZATION)
  } catch (e: Exception) {
    e.printStackTrace()
    Result.Error(DataError.Remote.UNKNOWN)
  }
}

suspend inline fun <reified T> responseToResult(
  response: HttpResponse
): Result<T, DataError.Remote> {
  return when(response.status.value) {
    in 200..299 -> {
      try {
        Result.Success(response.body<T>())
      } catch(e: NoTransformationFoundException) {
        e.printStackTrace()
        Result.Error(DataError.Remote.SERIALIZATION)
      }
    }
    408 -> Result.Error(DataError.Remote.REQUEST_TIMEOUT)
    429 -> Result.Error(DataError.Remote.TOO_MANY_REQUESTS)
    in 500..599 -> Result.Error(DataError.Remote.SERVER)
    else -> Result.Error(DataError.Remote.UNKNOWN)
  }
}