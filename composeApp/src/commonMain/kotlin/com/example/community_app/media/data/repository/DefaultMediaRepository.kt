package com.example.community_app.media.data.repository

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.MediaDto
import com.example.community_app.media.data.network.RemoteMediaDataSource
import com.example.community_app.media.domain.MediaRepository
import com.example.community_app.util.MediaTargetType

class DefaultMediaRepository(
  private val remoteDataSource: RemoteMediaDataSource
) : MediaRepository {
  override suspend fun getMediaList(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<MediaDto>, DataError.Remote> {
    return remoteDataSource.getMediaList(targetType, targetId)
  }
}