package com.example.community_app.media.domain

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.MediaDto
import com.example.community_app.util.MediaTargetType

interface MediaRepository {
  suspend fun getMediaList(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<MediaDto>, DataError.Remote>

  suspend fun downloadMedia(
    url: String,
    saveToFileName: String
  ): Result<String, DataError.Remote>

  suspend fun uploadMedia(
    targetType: MediaTargetType,
    targetId: Int,
    fileName: String
  ): Result<MediaDto, DataError.Remote>

  suspend fun deleteMedia(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int
  ): Result<Unit, DataError.Remote>

  suspend fun setCover(mediaId: Int): Result<MediaDto, DataError.Remote>
}