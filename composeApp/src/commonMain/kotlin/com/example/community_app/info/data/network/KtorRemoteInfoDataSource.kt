package com.example.community_app.info.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.InfoDto
import com.example.community_app.util.BASE_URL
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class KtorRemoteInfoDataSource(
  private val httpClient: HttpClient
): RemoteInfoDataSource {
  override suspend fun getInfos(): Result<List<InfoDto>, DataError.Remote> {
    return safeCall {
      httpClient.get(
        urlString = "$BASE_URL/api/info"
      ) {}
    }
  }
}