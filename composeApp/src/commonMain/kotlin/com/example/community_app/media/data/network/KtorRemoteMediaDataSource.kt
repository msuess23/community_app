package com.example.community_app.media.data.network

import com.example.community_app.core.data.http_client.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.MediaDto
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.forms.InputProvider
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.forms.formData
import io.ktor.client.request.put
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.core.Input

class KtorRemoteMediaDataSource (
  private val httpClient: HttpClient
) : RemoteMediaDataSource {
  override suspend fun getMediaList(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<MediaDto>, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/media/${targetType.name}/$targetId")
    }
  }

  override suspend fun downloadMedia(url: String): Result<ByteReadChannel, DataError.Remote> {
    return try {
      val response = httpClient.get("$BASE_URL$url")
      if (response.status.value in 200..299) {
        Result.Success(response.bodyAsChannel())
      } else {
        Result.Error(DataError.Remote.UNKNOWN)
      }
    } catch (e: Exception) {
      Result.Error(DataError.Remote.NO_INTERNET)
    }
  }

  @OptIn(InternalAPI::class)
  override suspend fun uploadMedia(
    targetType: MediaTargetType,
    targetId: Int,
    inputProvider: () -> Input,
    fileName: String,
    contentLength: Long
  ): Result<MediaDto, DataError.Remote> {
    return safeCall {
      httpClient.post("$BASE_URL/api/media/${targetType.name}/$targetId") {
        setBody(
          MultiPartFormDataContent(
            formData {
              append(
                "file",
                InputProvider(size = contentLength, block = inputProvider),
                Headers.build {
                  append(HttpHeaders.ContentType, "image/jpeg")
                  append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                }
              )
            }
          )
        )
      }
    }
  }

  override suspend fun deleteMedia(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int
  ): Result<Unit, DataError.Remote> {
    return safeCall {
      httpClient.delete("$BASE_URL/api/media/${targetType.name}/$targetId/$mediaId")
    }
  }

  override suspend fun setCover(mediaId: Int): Result<MediaDto, DataError.Remote> {
    return safeCall {
      httpClient.put("$BASE_URL/api/media/$mediaId/cover")
    }
  }
}