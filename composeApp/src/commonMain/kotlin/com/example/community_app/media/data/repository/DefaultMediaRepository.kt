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

  override suspend fun uploadMedia(
    targetType: MediaTargetType,
    targetId: Int,
    bytes: ByteArray
  ): Result<MediaDto, DataError.Remote> {
    return remoteDataSource.uploadMedia(targetType, targetId, bytes, "upload.jpg")
  }

  override suspend fun deleteMedia(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int
  ): Result<Unit, DataError.Remote> {
    return remoteDataSource.deleteMedia(targetType, targetId, mediaId)
  }

  override suspend fun setCover(mediaId: Int): Result<MediaDto, DataError.Remote> {
    return remoteDataSource.setCover(mediaId)
  }
}