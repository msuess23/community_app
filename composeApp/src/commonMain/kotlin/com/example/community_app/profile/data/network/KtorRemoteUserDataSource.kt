package com.example.community_app.profile.data.network

import com.example.community_app.core.data.http_client.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.UserDto
import com.example.community_app.dto.UserUpdateDto
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

class KtorRemoteUserDataSource(
  private val httpClient: HttpClient
): RemoteUserDataSource {

  override suspend fun getProfile(): Result<UserDto, DataError.Remote> {
    return safeCall {
      httpClient.get("$BASE_URL/api/user/me")
    }
  }

  override suspend fun updateProfile(request: UserUpdateDto): Result<UserDto, DataError.Remote> {
    return safeCall {
      httpClient.put("$BASE_URL/api/user/me") {
        contentType(ContentType.Application.Json)
        setBody(request)
      }
    }
  }
}