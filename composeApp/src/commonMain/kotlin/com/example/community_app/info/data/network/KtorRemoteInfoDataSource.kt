package com.example.community_app.info.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.info.data.dto.InfoDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get

private const val BASE_URL = "http://10.0.2.2:8080/api"

class KtorRemoteInfoDataSource(
  private val httpClient: HttpClient
): RemoteInfoDataSource {
  override suspend fun getInfos(): Result<List<InfoDto>, DataError.Remote> {
    return safeCall {
      httpClient.get(
        urlString = "$BASE_URL/info"
      ) {}
    }
  }
}