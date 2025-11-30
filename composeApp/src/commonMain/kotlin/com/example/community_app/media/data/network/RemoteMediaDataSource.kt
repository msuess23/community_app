package com.example.community_app.media.data.network

import com.example.community_app.core.domain.DataError
import com.example.community_app.core.domain.Result
import com.example.community_app.dto.MediaDto
import com.example.community_app.util.MediaTargetType
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.Input

interface RemoteMediaDataSource {
  suspend fun getMediaList(
    targetType: MediaTargetType,
    targetId: Int
  ): Result<List<MediaDto>, DataError.Remote>

  suspend fun downloadMedia(url: String): Result<ByteReadChannel, DataError.Remote>

  suspend fun uploadMedia(
    targetType: MediaTargetType,
    targetId: Int,
    inputProvider: () -> Input,
    fileName: String,
    contentLength: Long
  ): Result<MediaDto, DataError.Remote>

  suspend fun deleteMedia(
    targetType: MediaTargetType,
    targetId: Int,
    mediaId: Int
  ): Result<Unit, DataError.Remote>

  suspend fun setCover(mediaId: Int): Result<MediaDto, DataError.Remote>
}