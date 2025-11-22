package com.example.community_app.media.data.network

import com.example.community_app.core.data.safeCall
import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.MediaDto
import com.example.community_app.util.BASE_URL
import com.example.community_app.util.MediaTargetType
import io.ktor.client.HttpClient
import io.ktor.client.request.get

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
}